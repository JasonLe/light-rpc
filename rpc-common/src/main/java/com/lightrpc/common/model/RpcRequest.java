package com.lightrpc.common.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RpcRequest {

    /**
     * 请求号 (Long 类型，对应协议头的 8 bytes)
     */
    private Long requestId;

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数类型列表 (全限定类名，用于反射)
     */
    private String[] paramTypes;

    /**
     * 参数值列表
     */
    private Object[] parameters;
}