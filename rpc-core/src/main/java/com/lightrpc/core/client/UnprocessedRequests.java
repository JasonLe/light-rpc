package com.lightrpc.core.client;

import com.lightrpc.common.model.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequests {

    private static final ConcurrentHashMap<Long, CompletableFuture<RpcResponse>> unprocessedRequests = new ConcurrentHashMap<>();

    /**
     * 放入未处理的请求
     * @param requestId 请求ID
     * @param future    用于存放结果的 Future
     */
    public void put(Long requestId, CompletableFuture<RpcResponse> future) {
        unprocessedRequests.put(requestId, future);
    }

    /**
     * 完成请求
     * @param response 服务端返回的响应
     */
    public void complete(RpcResponse response) {
        CompletableFuture<RpcResponse> future = unprocessedRequests.remove(response.getRequestId());

        if (future != null) {
            future.complete(response);
        } else {
            // 这种情况可能是：服务端处理太慢，客户端已经超时并删除了 future，结果服务端才返回
            System.out.println("Received a response but no matching future found: " + response.getRequestId());
        }
    }
}