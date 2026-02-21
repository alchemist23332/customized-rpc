package com.yupi.yurpc.protocol;

import lombok.Getter;

@Getter
public enum ProtocolMessageStatusEnum {
    OK("ok", 20),
    BAD_REQUEST("badRequest", 40),
    BAD_RESPONSE("badResponse", 50);

    private final String text;
    private final int value;
    ProtocolMessageStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }
    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ProtocolMessageStatusEnum getByValue(int value) {
        for (ProtocolMessageStatusEnum status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }

}
