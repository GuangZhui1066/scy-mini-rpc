package com.minirpc.transport.netty;

import java.util.concurrent.CompletableFuture;

import com.minirpc.transport.InFlightRequests;
import com.minirpc.transport.ResponseFuture;
import com.minirpc.transport.Transport;
import com.minirpc.transport.command.Command;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Netty 实现网络通信
 */
public class NettyTransport implements Transport {

    /**
     * Netty 的 Channel 是在JDK的 NIO Channel 基础上进行封装的，屏蔽了底层的 Socket 连接
     */
    private final Channel channel;

    /**
     * 保存在途请求，即已经发出但没有收到响应的请求
     */
    private final InFlightRequests inFlightRequests;


    public NettyTransport(Channel channel, InFlightRequests inFlightRequests) {
        this.channel = channel;
        this.inFlightRequests = inFlightRequests;
    }


    @Override
    public CompletableFuture<Command> send(Command request) {
        // 构建请求返回值
        CompletableFuture<Command> responseFuture = new CompletableFuture<>();
        try {
            // 将在途请求放到 inFlightRequests 中
            inFlightRequests.put(new ResponseFuture(request.getHeader().getRequestId(), responseFuture));
            // 用 netty 发送请求数据
            channel
                .writeAndFlush(request)
                .addListener(new ChannelFutureListener() {
                    // 当 I/O 结束后回调
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (!channelFuture.isSuccess()) {
                            responseFuture.completeExceptionally(channelFuture.cause());
                            channel.close();
                        }
                    }
                });
        } catch (Throwable e) {
            // 发送异常
            inFlightRequests.remove(request.getHeader().getRequestId());
            responseFuture.completeExceptionally(e);
        }

        return responseFuture;
    }

}
