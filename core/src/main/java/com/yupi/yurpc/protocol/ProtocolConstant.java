package com.yupi.yurpc.protocol;

public interface ProtocolConstant {
    /**
     * 消息头长度
     */
    int MESSAGE_HEADER_LENGTH = 17;
    /**
     * 协议默认魔数
     */
    byte PROTOCOL_MAGIC = 0x1;
    /**
     * 协议默认版本号
     */
    byte PROTOCOL_VERSION = 0x1;

}
