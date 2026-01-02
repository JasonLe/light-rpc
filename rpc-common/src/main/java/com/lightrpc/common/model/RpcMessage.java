package com.lightrpc.common.model;

import lombok.Data;

@Data
public class RpcMessage {

    /**
     * 请求 ID (用于链路追踪和异步匹配)
     * 对应协议头的 Request ID
     */
    private Long requestId;

    /**
     * 消息类型 (1: 请求, 2: 响应, 3: 心跳)
     * 对应协议头的 Type 字段
     * com.lightrpc.common.enums.MessageTypeEnum
     */
    private byte messageType;

    /**
     * 序列化类型 (例如 1: JSON, 2: Protobuf)
     * 对应协议头的 Serializer 字段
     * com.lightrpc.common.enums.SerializerCodeEnum
     */
    private byte codec;

    /**
     * 压缩类型 (可选，预留扩展，例如 1: Gzip)
     */
    private byte compress;

    /**
     * 消息体数据
     * 在网络传输时，它是 RpcRequest 或 RpcResponse 序列化后的字节数组
     * 但在业务 handler 里，我们希望解码器已经把它转成了 Object
     */
    private Object data;
}