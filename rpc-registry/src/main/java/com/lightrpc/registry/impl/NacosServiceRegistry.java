package com.lightrpc.registry.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.lightrpc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {

    private NamingService namingService;

    /**
     * 构造函数：连接 Nacos
     * @param serverAddr Nacos 服务地址，例如 "127.0.0.1:8848"
     */
    public NacosServiceRegistry(String serverAddr) {
        try {
            // NamingFactory 是 Nacos 提供的工厂类，用于创建 NamingService
            namingService = NamingFactory.createNamingService(serverAddr);
        } catch (NacosException e) {
            log.error("连接 Nacos 发生异常: ", e);
            throw new RuntimeException("连接 Nacos 失败");
        }
    }

    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        int maxRetries = 5;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
                return; // 注册成功，直接返回
            } catch (Exception e) { // 捕获 NacosException 和 RuntimeException
                retryCount++;
                log.warn("服务注册失败，正在重试 ({}/{})... 异常信息: {}", retryCount, maxRetries, e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("服务注册线程被中断", interruptedException);
                }
            }
        }
        // 如果重试完还是失败
        throw new RuntimeException("服务注册失败，已重试 " + maxRetries + " 次");
    }

    @Override
    public InetSocketAddress lookup(String serviceName) {
        try {
            List<Instance> allInstances = namingService.getAllInstances(serviceName);

            if (allInstances == null || allInstances.isEmpty()) {
                log.error("未找到服务[{}]的可用实例", serviceName);
                return null;
            }

            // --- 负载均衡 (Load Balance) ---
            // 这里实现一个最简单的：随机 (Random)
            // 生产环境可以把这一块拆分成独立的 LoadBalancer 接口
            Instance instance = allInstances.get((int) (Math.random() * allInstances.size()));

            log.info("服务发现成功，负载均衡选择: {}:{}", instance.getIp(), instance.getPort());
            return new InetSocketAddress(instance.getIp(), instance.getPort());

        } catch (NacosException e) {
            log.error("服务发现失败", e);
            return null;
        }
    }
}
