package com.lightrpc.core.client;

import com.lightrpc.common.model.RpcMessage;
import com.lightrpc.core.codec.RpcMessageDecoder;
import com.lightrpc.core.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClient {

    private final String host;
    private final int port;
    private static final EventLoopGroup group = new NioEventLoopGroup();

    // 我们需要持有这个 channel，稍后用来发消息
    private Channel channel;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // Pipeline 必须和服务端保持一致（编解码器顺序）
                            ch.pipeline().addLast(new RpcMessageEncoder());
                            ch.pipeline().addLast(new RpcMessageDecoder());
                            ch.pipeline().addLast(new RpcClientHandler());
                        }
                    });

            // 连接服务端
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // 获取连接成功的 channel
            this.channel = future.channel();
            log.info("【客户端】已连接到服务端 {}:{}", host, port);

            // 注意：这里不能像 Server 那样调用 closeFuture().sync() 阻塞
            // 因为客户端连接成功后，主线程还要继续往下执行，发送数据！

        } catch (InterruptedException e) {
            log.error("连接失败", e);
        }
    }

    /**
     * 发送消息的方法
     */
    public void sendRequest(RpcMessage message) {
        this.channel.writeAndFlush(message);
    }
}