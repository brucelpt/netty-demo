package com.example.nettydemo.server;

import com.example.nettydemo.handler.HttpProxyFrontendHandler;
import com.example.nettydemo.ssl.SSLService;
import com.example.nettydemo.ssl.impl.SSLServiceImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpsServerProxy {
    private static final int LOCAL_PORT = 8801;
    // Replace with your server IP
    private static final String REMOTE_HOST = "10.30.51.70";
    private static final int REMOTE_PORT = 9808;

    private static final String[] ciphers = new String[]{"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"};

    public static void main(String[] args) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        SSLService sslService = new SSLServiceImpl();
        SSLContext sslContext = sslService.serverSSLConfigInit();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            SSLEngine engine = sslContext.createSSLEngine();
                            engine.setUseClientMode(false);
                            engine.setNeedClientAuth(true);
                            engine.setEnabledCipherSuites(ciphers);
                            SslHandler sslHandler = new SslHandler(engine);
                            sslHandler.setHandshakeTimeout(60000, TimeUnit.MILLISECONDS);
                            p.addLast("ssl", sslHandler);
                            p.addLast("codec", new HttpServerCodec());
                            p.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
                            p.addLast("handler", new HttpProxyFrontendHandler(REMOTE_HOST, REMOTE_PORT));
                        }
                    });
            ChannelFuture future = bootstrap.bind(LOCAL_PORT).sync();
            log.info("Proxy server started on port " + LOCAL_PORT);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


}
