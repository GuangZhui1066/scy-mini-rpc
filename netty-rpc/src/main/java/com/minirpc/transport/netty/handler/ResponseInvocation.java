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
     * 处理接收到的 I/O 事件
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Command response) {
        ResponseFuture future = inFlightRequests.remove(response.getHeader().getRequestId());
        if(null != future) {
            // 将 Future 状态设置为完成
            future.getFuture().complete(response);
        } else {
            logger.warn("Drop response: {}", response);
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
