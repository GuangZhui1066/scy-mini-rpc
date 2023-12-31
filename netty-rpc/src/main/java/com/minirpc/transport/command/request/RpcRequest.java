package com.minirpc.transport.command.request;

/**
 * 要发送到远程 (Provider) 的 RPC 请求的请求体
 */
public class RpcRequest {

    private final String interfaceName;

    private final String methodName;

    private final byte [] serializedArguments;


    public RpcRequest(String interfaceName, String methodName, byte[] serializedArguments) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.serializedArguments = serializedArguments;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public byte[] getSerializedArguments() {
        return serializedArguments;
    }

}
