package com.minirpc.serialize;

import java.util.Arrays;
import java.util.Optional;

import com.minirpc.nameservice.Metadata;
import com.minirpc.transport.command.request.RpcRequest;

/**
 * 需要被序列化的数据类型
 */
public enum SerialDataType {

    TYPE_STRING(0, String.class, "string"),

    TYPE_METADATA(100, Metadata.class, "metadata"),

    TYPE_RPC_REQUEST(101, RpcRequest.class, "rpc_request");


    private int code;
    private Class<?> clz;
    private String desc;

    SerialDataType(int code, Class<?> clz, String desc) {
        this.code = code;
        this.clz = clz;
        this.desc = desc;
    }

    public int getCode() {
        return this.code;
    }


    /**
     * 根据类型 code 找到对应的类型枚举
     */
    public static SerialDataType getSerialDataType(int code) {
        Optional<SerialDataType> dataType = Arrays.asList(SerialDataType.values())
            .stream()
            .filter(e -> code == e.code)
            .findFirst();
        if (dataType.isPresent()) {
            return dataType.get();
        }
        return null;
    }

    /**
     * 根据类型 class 找到对应的类型枚举
     */
    public static SerialDataType getSerialDataType(Class<?> clz) {
        Optional<SerialDataType> dataType = Arrays.asList(SerialDataType.values())
            .stream()
            .filter(e -> clz.isAssignableFrom(e.clz))
            .findFirst();
        if (dataType.isPresent()) {
            return dataType.get();
        }
        return null;
    }

}
