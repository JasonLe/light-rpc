package com.lightrpc.test;

import com.lightrpc.core.server.RpcServer;

import com.lightrpc.core.server.LocalRegistry;
import com.lightrpc.test.impl.UserServiceImpl;

/**
 * 测试服务端启动
 */
public class NettyTestServer {
    public static void main(String[] args) {
        // 注册服务
        LocalRegistry.register("com.lightrpc.api.user.UserService", new UserServiceImpl());

        // 模拟服务端启动，监听 6666 端口
        RpcServer server = new RpcServer("127.0.0.1", 6666);
        server.start();
    }
}