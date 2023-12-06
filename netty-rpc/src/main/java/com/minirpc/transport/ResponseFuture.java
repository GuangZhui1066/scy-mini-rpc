package com.minirpc.transport;

import java.util.concurrent.CompletableFuture;

import com.minirpc.transport.command.Command;

/**
 * 请求返回
 */
public class ResponseFuture {

    private final int requestId;

    private final CompletableFuture<Command> future;

    private final long timestamp;


    public ResponseFuture(int requestId, CompletableFuture<Command> future) {
        this.requestId = requestId;
        this.future = future;
        this.timestamp = System.nanoTime();
    }

    public int getRequestId() {
        return requestId;
    }

    public CompletableFuture<Command> getFuture() {
        return future;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
