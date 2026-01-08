package com.lightrpc.core.proxy;

import com.lightrpc.common.enums.MessageTypeEnum;
import com.lightrpc.common.enums.SerializerCodeEnum;
import com.lightrpc.common.model.RpcMessage;
import com.lightrpc.common.model.RpcRequest;
import com.lightrpc.common.model.RpcResponse;
import com.lightrpc.core.client.RpcClient;
import com.lightrpc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final ServiceRegistry serviceRegistry;

    public RpcClientProxy(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                this
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 构建请求体
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId(Math.abs(UUID.randomUUID().getMostSignificantBits()));
        rpcRequest.setInterfaceName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameters(args);
        rpcRequest.setParamTypes(getParameterTypes(method.getParameterTypes()));

        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setCodec(SerializerCodeEnum.JSON.getCode());
        rpcMessage.setMessageType(MessageTypeEnum.REQUEST.getType());
        rpcMessage.setRequestId(rpcRequest.getRequestId());
        rpcMessage.setData(rpcRequest);

        // 2. 服务发现：去 Nacos 查这个接口在哪台机器上
        InetSocketAddress address = serviceRegistry.lookup(rpcRequest.getInterfaceName());
        if (address == null) {
            throw new RuntimeException("未找到服务地址: " + rpcRequest.getInterfaceName());
        }

        CompletableFuture<RpcResponse> future = RpcClientFactory.sendRequest(
                address.getHostName(),
                address.getPort(),
                rpcMessage
        );
        RpcResponse response = future.get(); // 阻塞等待

        if (response.getCode() == null || response.getCode() != 200) {
            throw new RuntimeException("RPC调用失败: " + response.getMessage());
        }

        return response.getData();
    }

    private String[] getParameterTypes(Class<?>[] parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return new String[0];
        }
        String[] types = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            types[i] = parameterTypes[i].getName();
        }
        return types;
    }
}
