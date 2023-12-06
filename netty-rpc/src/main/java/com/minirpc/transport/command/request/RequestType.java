package com.minirpc.transport.command.request;

public enum RequestType {

    RPC_REQUEST(0, "rpc_request");


    private int code;
    private String type;

    RequestType(int code, String type) {
        this.code = code;
        this.type = type;
    }

    public int getCode() {
        return code;
    }

}
