package com.lightrpc.core.codec;

import com.lightrpc.common.model.RpcMessage;
import com.lightrpc.common.enums.SerializerCodeEnum;
import com.lightrpc.common.serializer.impl.JsonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Objects;

public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {

        byte[] bodyBytes = null;

        if (Objects.equals(SerializerCodeEnum.JSON.getCode(), rpcMessage.getCodec())) {
            JsonSerializer  jsonSerializer = new JsonSerializer();
            bodyBytes = jsonSerializer.serialize(rpcMessage.getData());
        }

        // 防止空指针, 如果序列化失败或 data 为空，写入空数组
        if (bodyBytes == null) {
            bodyBytes = new byte[0];
        }

        // 魔数 (4字节) - 用于校验协议是否合法
        byteBuf.writeInt(0xCAFEBABE)
                // 版本号(1字节)
                .writeByte(1)
                // 序列化算法(1字节)
                .writeByte(rpcMessage.getCodec())
                // 消息类型(1字节)
                .writeByte(rpcMessage.getMessageType())
                // 请求ID (8字节)
                .writeLong(rpcMessage.getRequestId())
                // 数据长度(4字节) - 告诉解码器后面要读多少数据
                .writeInt(bodyBytes.length)
                // 写入实际数据
                .writeBytes(bodyBytes);
    }
}
