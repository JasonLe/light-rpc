package com.lightrpc.common.enums;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    HEART((byte) 0),
    REQUEST((byte) 1),
    RESPONSE((byte) 2);

    /**
     * 消息类型
     */
    private byte type;

    MessageTypeEnum(byte type) {
        this.type = type;
    }
}
