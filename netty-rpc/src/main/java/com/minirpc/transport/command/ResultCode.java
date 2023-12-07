package com.minirpc.transport.command;

public enum ResultCode {

    SUCCESS(0, "SUCCESS"),
    NO_PROVIDER(-2, "NO_PROVIDER"),
    CANNOT_HANDLE(-3, "CANNOT_HANDLE"),
    UNKNOWN_ERROR(-1, "UNKNOWN_ERROR");

    private int code;
    private String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
