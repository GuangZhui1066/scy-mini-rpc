package com.minirpc.transport;

import java.util.concurrent.CompletableFuture;

import com.minirpc.transport.command.Command;

/**
 * 网络通信接口
 */
public interface Transport {

    /**
     * 发送请求命令，接收响应
     */
    CompletableFuture<Command> send(Command request);

}
