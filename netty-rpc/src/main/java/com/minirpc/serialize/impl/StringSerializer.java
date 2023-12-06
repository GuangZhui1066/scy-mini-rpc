package com.minirpc.serialize.impl;

import java.nio.charset.StandardCharsets;

import com.minirpc.serialize.SerialDataType;
import com.minirpc.serialize.Serializer;

public class StringSerializer implements Serializer<String> {

    @Override
    public SerialDataType dataType() {
        return SerialDataType.TYPE_STRING;
    }

    @Override
    public int size(String entry) {
        return entry.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public void serialize(String entry, byte[] bytes, int offset, int length) {
        byte [] strBytes = entry.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(strBytes, 0, bytes, offset, strBytes.length);
    }

    @Override
    public String parse(byte[] bytes, int offset, int length) {
        return new String(bytes, offset, length, StandardCharsets.UTF_8);
    }

}
