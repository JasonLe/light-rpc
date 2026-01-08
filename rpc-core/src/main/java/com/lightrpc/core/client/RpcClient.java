package com.lightrpc.core.client;

import com.lightrpc.common.model.RpcMessage;
import com.lightrpc.common.model.RpcResponse;
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
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcClient {

    private final String host;
    private final int port;
    private static final EventLoopGroup group = new NioEventLoopGroup();
    private final UnprocessedRequests unprocessedRequests;

    // 我们需要持有这个 channel，稍后用来发消息
    private Channel channel;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.unprocessedRequests = new UnprocessedRequests();
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

                            // 客户端检测写空闲
                            // 如果 5 秒没有向服务端发送数据，触发 WRITER_IDLE 事件
//                            ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));

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
    public CompletableFuture<RpcResponse> sendRequest(RpcMessage message) {
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        unprocessedRequests.put(message.getRequestId(), resultFuture);
        this.channel.writeAndFlush(message);
        return resultFuture;
    }
}