package com.minirpc.transport.command;

import java.nio.charset.StandardCharsets;

/**
 * 请求的返回头
 */
public class ResponseHeader extends Header {

    private int code;
    private String errMsg;


    public ResponseHeader(int type, int version, int requestId) {
        this(type, version, requestId, ResultCode.SUCCESS.getCode(), null);
    }

    public ResponseHeader(int type, int version, int requestId, int code, String errMsg) {
        super(type, version, requestId);
        this.code = code;
        this.errMsg = errMsg;
    }


    /**
     * 固定部分 (type + version + requestId + code) 序列化后的长度，即字节数
     */
    public static int fixedLength() {
        return Integer.BYTES + Integer.BYTES + Integer.BYTES + Integer.BYTES;
    }

    /**
     * 编码后的长度，即字节数
     * 固定部分 + errMsg长度 + errMsg
     */
    @Override
    public int encodedLength() {
        return fixedLength() + (errMsg == null ? 0 : errMsg.getBytes(StandardCharsets.UTF_8).length);
    }



    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

}
