package com.minirpc.serialize.impl.fastjson.impl;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.minirpc.nameservice.Metadata;
import com.minirpc.serialize.SerialDataType;
import com.minirpc.serialize.impl.fastjson.JSONSerializer;

public class MetadataSerializer extends JSONSerializer<Metadata> {

    @Override
    public SerialDataType dataType() {
        return SerialDataType.TYPE_METADATA;
    }

    @Override
    public void serialize(Metadata entry, byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
        byte[] jsonBytes = JSON.toJSONString(entry).getBytes(StandardCharsets.UTF_8);
        buffer.putInt(jsonBytes.length);
        buffer.put(jsonBytes);
    }

    @Override
    public Metadata parse(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
        int sizeOfJson = buffer.getInt();
        byte[] jsonBytes = new byte[sizeOfJson];
        buffer.get(jsonBytes);
        String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
        JSONObject jsonObject = JSON.parseObject(jsonString);
        Metadata metadata = new Metadata();
        jsonObject.entrySet()
            .forEach(entry -> {
                List<String> uriList = (List<String>) entry.getValue();
                List<URI> uris = uriList.stream()
                    .map(URI::create)
                    .collect(Collectors.toList());
                metadata.put(entry.getKey(), uris);
            });
        return metadata;
    }

}
