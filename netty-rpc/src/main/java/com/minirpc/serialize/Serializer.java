package com.minirpc.serialize;

/**
 * 序列化器接口
 *   特定类型的对象  <<<=== 序列化/反序列化 ===>>>  字符数组
 *
 * 有三个实现类，每个实现类实现一种数据的序列化：
 *   StringSerializer，用于序列化 String 类型的数据
 *   MetadataSerializer，用于序列化注册中心元数据 Metadata 类型
 *   RpcRequestSerializer，用于序列化RPC请求 RpcRequest 类型
 */
public interface Serializer<T> {

    /**
     * 需要序列化的数据类型
     */
    SerialDataType dataType();

    /**
     * 计算对象序列化后的长度，主要用于申请存放序列化数据的字节数组
     * @param entry 待序列化的对象
     * @return 对象序列化后的长度
     */
    int size(T entry);

    /**
     * 序列化对象。将给定的对象序列化成字节数组
     * @param entry 待序列化的对象
     * @param bytes 存放序列化数据的字节数组
     * @param offset bytes数组的偏移量，从这个位置开始写入序列化数据
     * @param length 对象序列化后的长度
     */
    void serialize(T entry, byte[] bytes, int offset, int length);

    /**
     * 反序列化对象
     * @param bytes 存放序列化数据的字节数组
     * @param offset bytes数组的偏移量
     * @param length 对象序列化后的长度
     * @return 反序列化之后生成的对象
     */
    T parse(byte[] bytes, int offset, int length);

}

