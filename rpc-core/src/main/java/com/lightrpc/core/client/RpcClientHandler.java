package com.lightrpc.core.client;

import com.lightrpc.common.enums.MessageTypeEnum;
import com.lightrpc.common.model.RpcMessage;
import com.lightrpc.common.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("客户端异常", cause);
        ctx.close();
    }
}