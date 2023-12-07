package com.minirpc.serialize.impl.fastjson;

import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSON;

import com.minirpc.serialize.Serializer;

/**
 * JSON 实现序列化
 */
public abstract class JSONSerializer<T> implements Serializer<T> {

    @Override
    public int size(T entry) {
        return Integer.BYTES + JSON.toJSONString(entry).getBytes(StandardCharsets.UTF_8).length;
    }

}
