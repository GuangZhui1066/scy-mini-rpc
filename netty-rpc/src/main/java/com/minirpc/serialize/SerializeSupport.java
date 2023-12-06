package com.minirpc.serialize;

import java.util.HashMap;
import java.util.Map;

import com.minirpc.api.spi.ServiceSupport;

/**
 * 序列化工具类
 *   提供结构化数据与字节数组之间的序列化 / 反序列化能力
 *   序列化之后的字节数组中，第一个字节表示数据类型
 */
public class SerializeSupport {

    /**
     * 存放 <需要序列化的对象类型, 此类型对应的序列化实现类>
     */
    private static Map<SerialDataType, Serializer> serializerMap = new HashMap<>();

    static {
        for (Serializer<?> serializer : ServiceSupport.loadAll(Serializer.class)) {
            serializerMap.put(serializer.dataType(), serializer);
        }
    }


    /**
     * 序列化
     */
    public static <E> byte [] serialize(E entry) {
        SerialDataType serialDataType = SerialDataType.getSerialDataType(entry.getClass());
        Serializer<E> serializer = (Serializer<E>) serializerMap.get(serialDataType);
        if(serializer == null) {
            throw new RuntimeException("Serialize error. unknown class type: " + entry.getClass().toString());
        }
        byte [] bytes = new byte[serializer.size(entry) + 1];
        bytes[0] = (byte) serializer.dataType().getCode();
        serializer.serialize(entry, bytes, 1, bytes.length - 1);
        return bytes;
    }


    /**
     * 反序列化
     */
    public static <E> E parse(byte [] buffer) {
        return parse(buffer, 0, buffer.length);
    }

    private static <E> E parse(byte[] buffer, int offset, int length) {
        // 读取第一个字节 (数据类型的标识位)
        byte type = buffer[0];
        // 根据类型标识位找到对应的类型
        SerialDataType serialDataType = SerialDataType.getSerialDataType(type);
        if (null == serialDataType) {
            throw new RuntimeException("Unknown serial type: " + type);
        }
        // 把字节数组反序列化成指定类型
        return parse(buffer, offset + 1, length - 1, serialDataType);
    }

    private static <E> E parse(byte [] buffer, int offset, int length, SerialDataType dataType) {
        // 根据数据类型找到对应的序列化器
        Serializer serializer = serializerMap.get(dataType);
        // 执行反序列化
        return (E) serializer.parse(buffer, offset, length);
    }

}
