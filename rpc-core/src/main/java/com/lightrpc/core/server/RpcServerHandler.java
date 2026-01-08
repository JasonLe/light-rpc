package com.lightrpc.core.server;

import com.lightrpc.common.enums.MessageTypeEnum;
import com.lightrpc.common.model.RpcMessage;
import com.lightrpc.common.model.RpcRequest;
import com.lightrpc.common.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        // 如果能打印出这个日志，说明：
        // 1. 数据从网卡进来了
        // 2. LengthFieldBasedFrameDecoder 解决了粘包
        // 3. RpcMessageDecoder 反序列化成功了
        log.info("【服务端】接收到消息: {}", msg);

        // 如果是心跳包 (假设类型 3 是心跳)
        if (msg.getMessageType() == 3) {
            log.info("【服务端】接收到心跳检测...");
            return;
        }

        // 1. 获取 msg 中的 RpcRequest
        RpcRequest request = (RpcRequest) msg.getData();
        
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        
        try {
            // 2. 获取服务实现类
            Object service = LocalRegistry.get(request.getInterfaceName());
            if (service == null) {
                throw new RuntimeException("Service not found: " + request.getInterfaceName());
            }
            
            // 处理参数类型
            Class<?>[] parameterTypes = new Class[request.getParamTypes().length];
            for (int i = 0; i < request.getParamTypes().length; i++) {
                parameterTypes[i] = Class.forName(request.getParamTypes()[i]);
            }

            // 3. 通过反射调用真实服务
            java.lang.reflect.Method method = service.getClass().getMethod(request.getMethodName(), parameterTypes);
            Object result = method.invoke(service, request.getParameters());
            
            response.setCode(200);
            response.setMessage("success");
            response.setData(result);
            
        } catch (Exception e) {
            log.error("RPC调用执行失败", e);
            response.setCode(500);
            response.setMessage("fail: " + e.getMessage());
        }

        // 4. 封装 RpcResponse 发回给客户端
        // 复用原来的 RpcMessage 或者新建一个
        RpcMessage responseMessage = new RpcMessage();
        responseMessage.setCodec(msg.getCodec());
        responseMessage.setMessageType(MessageTypeEnum.RESPONSE.getType()); // Response
        responseMessage.setRequestId(msg.getRequestId());
        responseMessage.setData(response);
        
        ctx.writeAndFlush(responseMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("服务端异常", cause);
        ctx.close();
    }
}