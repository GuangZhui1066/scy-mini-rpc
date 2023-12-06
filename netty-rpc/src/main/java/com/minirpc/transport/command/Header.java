package com.minirpc.transport.command;

/**
 * 请求头
 */
public class Header {

    /**
     *
     */
    private int type;

    /**
     * 请求版本
     */
    private int version;

    /**
     * 唯一的请求id
     */
    private int requestId;


    public Header(int type, int version, int requestId) {
        this.type = type;
        this.version = version;
        this.requestId = requestId;
    }

    /**
     * 编码后的长度，即字节数
     *   type + version + requestId
     */
    public int encodedLength() {
        return Integer.BYTES + Integer.BYTES + Integer.BYTES;
    }


    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
