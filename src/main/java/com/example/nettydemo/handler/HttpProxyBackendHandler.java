package com.example.nettydemo.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpProxyBackendHandler extends ChannelInboundHandlerAdapter {

    private Channel inboundChannel;

    public HttpProxyBackendHandler(Channel serverInboundChannel) {
        inboundChannel = serverInboundChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("-----Receive data returned by the background server-----");
        inboundChannel.writeAndFlush(msg);
        log.info("-----Finish writing data back-----");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.error("------HttpProxyBackendHandler channelInactive----------");
        if (inboundChannel != null) {
            closeOnFlush(inboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("------HttpProxyBackendHandler exceptionCaught----------");
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    /**
     * 在刷新所有排队的写请求后关闭指定的通道。
     */
    private void closeOnFlush(Channel ch) {
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

}
