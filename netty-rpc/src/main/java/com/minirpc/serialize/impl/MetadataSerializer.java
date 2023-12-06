package com.minirpc.serialize.impl;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.minirpc.nameservice.Metadata;
import com.minirpc.serialize.SerialDataType;
import com.minirpc.serialize.Serializer;

public class MetadataSerializer implements Serializer<Metadata> {

    @Override
    public SerialDataType dataType() {
        return SerialDataType.TYPE_METADATA;
    }

    @Override
    public int size(Metadata entry) {
        return Short.BYTES + entry.entrySet().stream().mapToInt(this::entrySize).sum();
    }

    private int entrySize(Map.Entry<String, List<URI>> e) {
        return Short.BYTES + e.getKey().getBytes().length +
               Short.BYTES + e.getValue().stream().mapToInt(uri -> Short.BYTES + uri.toASCIIString().getBytes(StandardCharsets.UTF_8).length).sum();
    }


    @Override
    public void serialize(Metadata entry, byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
        buffer.putShort(toShortSafely(entry.size()));

        entry.forEach((k, v) -> {
            byte [] keyBytes = k.getBytes(StandardCharsets.UTF_8);
            buffer.putShort(toShortSafely(keyBytes.length));
            buffer.put(keyBytes);

            buffer.putShort(toShortSafely(v.size()));
            for (URI uri : v) {
                byte [] uriBytes = uri.toASCIIString().getBytes(StandardCharsets.UTF_8);
                buffer.putShort(toShortSafely(uriBytes.length));
                buffer.put(uriBytes);
            }
        });
    }


    @Override
    public Metadata parse(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);

        Metadata metadata = new Metadata();
        int sizeOfMap = buffer.getShort();
        for (int i = 0; i < sizeOfMap; i++) {
            int keyLength = buffer.getShort();
            byte [] keyBytes = new byte [keyLength];
            buffer.get(keyBytes);
            String key = new String(keyBytes, StandardCharsets.UTF_8);

            int uriListSize = buffer.getShort();
            List<URI> uriList = new ArrayList<>(uriListSize);
            for (int j = 0; j < uriListSize; j++) {
                int uriLength = buffer.getShort();
                byte [] uriBytes = new byte [uriLength];
                buffer.get(uriBytes);
                URI uri  = URI.create(new String(uriBytes, StandardCharsets.UTF_8));
                uriList.add(uri);
            }
            metadata.put(key, uriList);
        }
        return metadata;
    }


    private short toShortSafely(int v) {
        assert v < Short.MAX_VALUE;
        return (short) v;
    }

}
