package com.lightrpc.core.proxy;

import com.lightrpc.common.model.RpcMessage;
import com.lightrpc.common.model.RpcResponse;
import com.lightrpc.core.client.RpcClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端连接工厂 (单例)
 * 负责复用 RpcClient，避免重复建立连接
 */
@Slf4j
public class RpcClientFactory {

    // 缓存：Key 是 "IP:Port", Value 是建立好的 RpcClient
    private static final Map<String, RpcClient> CLIENT_CACHE = new ConcurrentHashMap<>();

    /**
     * 发送请求（自动复用连接）
     */
    public static CompletableFuture<RpcResponse> sendRequest(String host, int port, RpcMessage message) {
        String addressKey = host + ":" + port;

        // computeIfAbsent: 如果缓存里有，直接返回；没有则执行后面的 lambda 创建并放入缓存
        RpcClient client = CLIENT_CACHE.computeIfAbsent(addressKey, key -> {
            log.info("创建新的连接: {}", key);
            RpcClient newClient = new RpcClient(host, port);
            // 建立连接
            newClient.connect();
            return newClient;
        });

        // 此时 client 已经是连接状态，直接发送即可
        // TODO：这里其实还缺少一个环节：如果连接断开了（服务端挂了），需要移除缓存重连。
        // 这一步我们放到后面的"心跳检测"环节去完善。

        return client.sendRequest(message);
    }
}