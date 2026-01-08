package com.lightrpc.core.codec;

import com.lightrpc.common.enums.MessageTypeEnum;
import com.lightrpc.common.model.RpcMessage;
import com.lightrpc.common.model.RpcRequest;
import com.lightrpc.common.model.RpcResponse;
import com.lightrpc.common.enums.SerializerCodeEnum;
import com.lightrpc.common.serializer.impl.JsonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        // maxFrameLength: 8MB
        // lengthFieldOffset: 15 (魔数4+版本1+序列化1+类型1+请求ID8)
        // lengthFieldLength: 4
        // lengthAdjustment: 0 (长度字段只包含消息体长度，不需要修正)
        // initialBytesToStrip: 0 (我们需要读取 Header 信息，所以不跳过任何字节)
        super(8 * 1024 * 1024, 15, 4, 0, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 如果数据不够，这里会返回 null，Netty 会继续等待数据
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);

        if (frame == null) {
            return null;
        }

        // 2. 开始解析
        RpcMessage rpcMessage = new RpcMessage();

        // 逐个读取 Header (必须和 Encoder 的写入顺序完全一致)
        int magic = frame.readInt(); // 魔数
        if (magic != 0xCAFEBABE) {
            throw new IllegalArgumentException("魔数非法: " + magic);
        }

        byte version = frame.readByte(); // 版本
        byte codec = frame.readByte(); // 序列化算法
        byte messageType = frame.readByte(); // 消息类型
        long requestId = frame.readLong(); // 请求ID
        int length = frame.readInt(); // 数据长度

        // 填充到对象中
        rpcMessage.setCodec(codec);
        rpcMessage.setMessageType(messageType);
        rpcMessage.setRequestId(requestId);

        // 3. 读取 Body
        if (length > 0) {
            byte[] bodyBytes = new byte[length];
            frame.readBytes(bodyBytes);

            // 4. 反序列化
            if (codec == SerializerCodeEnum.JSON.getCode()) {
                JsonSerializer serializer = new JsonSerializer();

                // 关键点：根据消息类型，决定反序列化成 Request 还是 Response
                if (messageType == MessageTypeEnum.REQUEST.getType()) {
                    RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
                    rpcMessage.setData(request);
                } else if (messageType == MessageTypeEnum.RESPONSE.getType()) {
                    RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                    rpcMessage.setData(response);
                }
            }
        }

        return rpcMessage;
    }
}