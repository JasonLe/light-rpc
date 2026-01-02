package com.lightrpc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SerializerCodeEnum {

    JSON((byte) 1),
    PROTOBUF((byte) 2); // 预留

    private final byte code;
}