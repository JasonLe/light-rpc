package com.lightrpc.common.serializer;

public interface Serializer {

    /**
     * 序列化
     * @param object
     * @return
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     * @param bytes
     * @param clazz
     * @return
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
