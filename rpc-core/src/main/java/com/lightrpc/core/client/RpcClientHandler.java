package com.lightrpc.core.client;

import com.lightrpc.common.model.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        // 这一步在 Week 1 暂时还没用，因为服务端目前只打印不返回。
        // 等到了 Week 2，我们会在这里处理服务端的 RpcResponse
        log.info("【客户端】收到服务端响应: {}", msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("客户端异常", cause);
        ctx.close();
    }
}