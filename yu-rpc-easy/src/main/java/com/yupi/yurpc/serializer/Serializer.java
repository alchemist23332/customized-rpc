package com.yupi.yurpc.serializer;

import java.io.IOException;

public interface Serializer {
    /**
     * 序列化
     *
     * @param obj
     * @return
     */
    <T> byte[] serialize(T obj) throws IOException;

    /**
     * 反序列化
     *
     * @param bytes
     * @param clazz
     * @return
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException;

}
