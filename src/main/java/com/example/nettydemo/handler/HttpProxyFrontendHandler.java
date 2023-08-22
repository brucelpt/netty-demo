package com.example.nettydemo.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpProxyFrontendHandler extends ChannelInboundHandlerAdapter {

    private String REMOTE_HOST = "";
    private int REMOTE_PORT = 0;
    private Channel outboundChannel;

    // 客户端到代理的 channel
    private Channel clientChannel;

    public HttpProxyFrontendHandler(String host, int port) {
        REMOTE_HOST = host;
        REMOTE_PORT = port;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clientChannel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("---channelRead begin---");
//        log.info("msg: " + msg.toString());
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            ChannelFuture f = null;
            // 开始和后端业务服务器建链
            Channel inboundChannel = ctx.channel();
            log.info("---Start connecting with the background server---");
            // 开始建立连接
            Bootstrap b = new Bootstrap();
            b.group(clientChannel.eventLoop())
                    .channel(ctx.channel().getClass())
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new HttpClientCodec());
                            p.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
                            p.addLast("handler", new HttpProxyBackendHandler(inboundChannel));
                        }
                    });
            f = b.connect(REMOTE_HOST, REMOTE_PORT);
            outboundChannel = f.channel();
            f.addListener(future -> {
                if (future.isSuccess()) {
                    log.info("---The connection is successful and data transfer begins---");
                    outboundChannel.writeAndFlush(fullHttpRequest);
                    log.info("---The connection is successful and the data transfer is complete---");
                } else {
                    // 如果连接请求失败，请关闭连接。
                    inboundChannel.close();
                    log.error("Connection failed！ IP: " + REMOTE_HOST + " port: " + REMOTE_PORT);
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.error("------进入 HttpProxyFrontendHandler channelInactive----------");
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("------进入 HttpProxyFrontendHandler exceptionCaught----------");
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    /**
     * 在刷新所有排队的写请求后关闭指定的通道。
     */
    static void closeOnFlush(Channel ch) {
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
