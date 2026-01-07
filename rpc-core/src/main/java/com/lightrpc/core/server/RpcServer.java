package com.lightrpc.core.server;

import com.lightrpc.core.codec.RpcMessageDecoder;
import com.lightrpc.core.codec.RpcMessageEncoder;
import com.lightrpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class RpcServer {

    private final String host;
    private final int port;
    // 注册中心实现类
    private final ServiceRegistry serviceRegistry;

    public RpcServer(String host, int port, ServiceRegistry serviceRegistry) {
        this.host = host;
        this.port = port;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * 发布服务的方法
     * 1. 注册到本地 LocalRegistry (供 ServerHandler 反射调用)
     * 2. 注册到 Nacos (供 Client 发现)
     */
    public <T> void publishService(String serviceName, Object serviceBean) {
        // 1. 本地注册
        LocalRegistry.register(serviceName, serviceBean);

        // 2. 远程注册 (把本机 IP 和端口告诉 Nacos)
        if (serviceRegistry != null) {
            serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
        }
    }

    public void start() {
        // 1. 创建 Boss 线程组：只负责处理“连接请求”
        // 这里的参数 1 表示只用 1 个线程去监听端口（对于服务端通常足够了）
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);

        // 2. 创建 Worker 线程组：负责具体的“IO 读写”和“业务处理”
        // 默认线程数是 CPU 核数 * 2
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 3. 创建服务端启动助手
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 4. 配置启动参数
            serverBootstrap.group(bossGroup, workerGroup)
                    // 指定 IO 模型为 NIO (Non-blocking IO)
                    .channel(NioServerSocketChannel.class)
                    // TCP 参数：SO_BACKLOG - 握手请求的队列大小
                    // 如果同时来了 1000 个连接，先放在队列里排队，队列满了就拒绝
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // TCP 参数：SO_KEEPALIVE - 开启 TCP 底层的心跳机制
                    // 超过 2 小时没有数据传输，TCP 会自动发送探测包看连接是否还活着
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // TCP 参数：TCP_NODELAY - 禁用 Nagle 算法
                    // Nagle 算法会把小数据包凑成大包再发，虽然省流量但延迟高。RPC 需要低延迟，所以要禁用（设为 true）
                    .childOption(ChannelOption.TCP_NODELAY, true)

                    // 5. 初始化 Pipeline (流水线)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 添加入站和出站的 Handler
                            // 顺序非常重要！

                            // 编码器 (Outbound): 发送数据时，把对象变成字节
                            ch.pipeline().addLast(new RpcMessageEncoder());

                            // 解码器 (Inbound): 接收数据时，把字节变成对象 (处理粘包)
                            ch.pipeline().addLast(new RpcMessageDecoder());

                            // 业务处理器 (Inbound): 真正的 RPC 业务逻辑
                            ch.pipeline().addLast(new RpcServerHandler());
                        }
                    });

            // 6. 绑定端口，同步等待绑定成功
            // sync() 会阻塞当前线程，直到绑定完成
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(host, port)).sync();

            log.info("【服务端】启动成功，监听地址: {}:{}", host, port);

            // 7. 等待服务端监听端口关闭
            // 这行代码会让主线程阻塞在这里，不会让程序运行完就直接退出
            // 只有当 channel 关闭时（比如 server 被 stop），这行代码才会放行
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("服务端启动时发生异常", e);
            Thread.currentThread().interrupt();
        } finally {
            // 8. 优雅停机
            // 无论发生什么异常，或者 server 关闭，都要正确释放线程资源
            log.info("服务端正在关闭...");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}