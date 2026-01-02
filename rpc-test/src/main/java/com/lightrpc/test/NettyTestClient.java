package com.lightrpc.test;

import com.lightrpc.common.model.RpcMessage;
import com.lightrpc.common.model.RpcRequest;
import com.lightrpc.common.enums.SerializerCodeEnum;
import com.lightrpc.core.client.RpcClient;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试客户端发送请求
 */
@Slf4j
public class NettyTestClient {
    public static void main(String[] args) {
        // 1. 启动客户端连接
        RpcClient client = new RpcClient("127.0.0.1", 6666);
        client.connect();

        // 2. 模拟一个 RPC 请求对象
        // 假设我们要调用 UserService.getUser("sichuan_boy")
        RpcRequest request = new RpcRequest();
        request.setRequestId(10086L); // 生成一个请求ID
        request.setInterfaceName("com.lightrpc.api.user.UserService"); // 接口全限定名
        request.setMethodName("getUser");
        request.setParameters(new Object[]{"亨利"}); // 参数值
        request.setParamTypes(new String[]{"java.lang.String"});   // 参数类型

        // 3. 包装成协议消息 (Message)
        RpcMessage message = new RpcMessage();
        message.setCodec(SerializerCodeEnum.JSON.getCode()); // 指定使用 JSON 序列化
        message.setMessageType((byte) 1); // 1 表示这是一个 Request 请求
        message.setRequestId(10086L);     // 协议头里的 ID，用于匹配
        message.setData(request);         // 消息体

        // 4. 发送消息
        client.sendRequest(message);

        // 为了防止主线程发完就退出，导致看不到日志，稍微 sleep 一下
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("消息发送成功");
    }
}