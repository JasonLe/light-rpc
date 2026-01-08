package com.lightrpc.core.client;

import com.lightrpc.common.enums.MessageTypeEnum;
import com.lightrpc.common.enums.SerializerCodeEnum;
import com.lightrpc.common.model.RpcMessage;
import com.lightrpc.common.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private final UnprocessedRequests unprocessedRequests;

    public RpcClientHandler() {
        this.unprocessedRequests = new UnprocessedRequests();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        log.info("【客户端】收到服务端响应: {}", msg);
        if (msg.getMessageType() == MessageTypeEnum.RESPONSE.getType()) {
            RpcResponse response = (RpcResponse) msg.getData();
            unprocessedRequests.complete(response);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("【客户端】5秒未发送数据，发送心跳包 Ping...");

                // 构建心跳包
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializerCodeEnum.JSON.getCode());
                rpcMessage.setMessageType(MessageTypeEnum.HEART.getType());
                // 心跳包不需要 RequestId 和 Data，只有头信息就够了
                rpcMessage.setRequestId(0L);

                ctx.writeAndFlush(rpcMessage);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("客户端异常", cause);
        ctx.close();
    }
}