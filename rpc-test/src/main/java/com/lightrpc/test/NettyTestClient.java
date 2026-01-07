package com.lightrpc.test;

import com.lightrpc.api.user.UserService;
import com.lightrpc.core.client.RpcClient;
import com.lightrpc.core.proxy.RpcClientProxy;
import com.lightrpc.registry.ServiceRegistry;
import com.lightrpc.registry.impl.NacosServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 测试客户端发送请求 (使用动态代理)
 */
@Slf4j
public class NettyTestClient {
    public static void main(String[] args) {

        ServiceRegistry registry = new NacosServiceRegistry("127.0.0.1:8848");

        // 2. 创建代理对象
        RpcClientProxy proxy = new RpcClientProxy(registry);
        UserService userService = proxy.getProxy(UserService.class);

        // 3. 调用远程方法
        for (int i = 0; i < 5; i++) {
            try {
                String result = userService.getUser("LightRPC-" + i);
                log.info("RPC 调用结果: {}", result);
                Thread.sleep(1000); // 稍微停顿一下
            } catch (Exception e) {
                log.error("RPC 调用异常", e);
            }
        }
    }
}