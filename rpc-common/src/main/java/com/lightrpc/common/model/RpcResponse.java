package com.lightrpc.common.model;

import lombok.Data;

@Data
public class RpcResponse {

    private Long requestId;

    private Integer code;

    private String message;

    private Object data;
}
