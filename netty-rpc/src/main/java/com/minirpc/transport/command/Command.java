package com.minirpc.transport.command;

/**
 * 网络请求
 */
public class Command {

    /**
     * 请求头
     */
    protected Header header;

    /**
     * 序列化后的请求体
     */
    private byte [] payload;


    public Command(Header header, byte [] payload) {
        this.header = header;
        this.payload = payload;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public byte [] getPayload() {
        return payload;
    }

    public void setPayload(byte [] payload) {
        this.payload = payload;
    }

}
