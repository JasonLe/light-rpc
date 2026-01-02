package com.lightrpc.common.serializer.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lightrpc.common.serializer.Serializer;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class JsonSerializer implements Serializer {

    private static final Gson gson = new GsonBuilder()
            .serializeNulls() // 允许序列化 null 值，看需求
            .setDateFormat("yyyy-MM-dd HH:mm:ss") // 设置时间格式
            .create();

    @Override
    public byte[] serialize(Object object) {
        if (Objects.isNull(object)) {
            return new byte[0];
        }
        String json = gson.toJson(object);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        String json = new String(bytes, StandardCharsets.UTF_8);
        return gson.fromJson(json, clazz);
    }
}
