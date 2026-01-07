package com.lightrpc.registry;

import java.net.InetSocketAddress;

/**
 * 注册中心接口
 * 定义通用的注册和服务发现行为
 */
public interface ServiceRegistry {
    /**
     * 注册服务
     * @param serviceName 服务名称（通常是接口全类名）
     * @param inetSocketAddress 服务地址（IP:Port）
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);

    /**
     * 服务发现
     * @param serviceName 服务名称
     * @return 服务地址
     */
    InetSocketAddress lookup(String serviceName);
}
