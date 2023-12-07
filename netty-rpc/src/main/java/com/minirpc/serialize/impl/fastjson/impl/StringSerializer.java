package com.minirpc.serialize.impl.fastjson.impl;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSON;

import com.minirpc.serialize.SerialDataType;
import com.minirpc.serialize.impl.fastjson.JSONSerializer;

public class StringSerializer extends JSONSerializer<String> {

    @Override
    public SerialDataType dataType() {
        return SerialDataType.TYPE_STRING;
    }

    @Override
    public void serialize(String entry, byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
        byte[] jsonBytes = JSON.toJSONString(entry).getBytes(StandardCharsets.UTF_8);
        buffer.putInt(jsonBytes.length);
        buffer.put(jsonBytes);
    }

    @Override
    public String parse(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
        int sizeOfJson = buffer.getInt();
        byte[] jsonBytes = new byte[sizeOfJson];
        buffer.get(jsonBytes);
        return JSON.parseObject(jsonBytes, String.class);
    }

}
