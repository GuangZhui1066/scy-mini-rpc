package com.minirpc.transport.netty.handler;

import com.minirpc.transport.InFlightRequests;
import com.minirpc.transport.ResponseFuture;
import com.minirpc.transport.command.Command;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class ResponseInvocation extends SimpleChannelInboundHandler<Command> {

    private static final Logger logger = LoggerFactory.getLogger(ResponseInvocation.class);

    private final InFlightRequests inFlightRequests;

    public ResponseInvocation(InFlightRequests inFlightRequests) {
        this.inFlightRequests = inFlightRequests;
    }


    /**
     * Consumer 端处理接收到的 I/O 事件 (即收到 RPC 的返回响应)
     *   Consumer 在这里异步接收 Provider 的响应，收到响应后结束掉 responseFuture
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Command response) {
        // 收到响应后，从在途请求中移除
        ResponseFuture responseFuture = inFlightRequests.remove(response.getHeader().getRequestId());
        if (null != responseFuture) {
            // 将 responseFuture 状态设置为完成，将响应中的返回值付赋给 responseFuture
            responseFuture.getFuture().complete(response);
        } else {
            // 已经因返回超时被 inFlightRequests 定期清除
            logger.warn("请求超时！Request timeout, response dropped.");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Exception: ", cause);
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }

}
