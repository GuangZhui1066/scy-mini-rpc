package com.minirpc.transport.netty.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.minirpc.server.RequestHandler;
import com.minirpc.server.RequestHandlerRegistry;
import com.minirpc.transport.command.Command;
import com.minirpc.transport.command.ResponseHeader;
import com.minirpc.transport.command.ResultCode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class RequestInvocation extends SimpleChannelInboundHandler<Command> {

    private static final Logger logger = LoggerFactory.getLogger(RequestInvocation.class);

    private final RequestHandlerRegistry requestHandlerRegistry;

    /**
     * 用于处理请求的线程池
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);


    public RequestInvocation(RequestHandlerRegistry requestHandlerRegistry) {
        this.requestHandlerRegistry = requestHandlerRegistry;
    }


    /**
     * Provider 端处理接收到的 I/O 事件 (即收到 RPC 的请求)
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Command requestCommand) throws Exception {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                RequestHandler handler = requestHandlerRegistry.get(requestCommand.getHeader().getType());
                Command responseCommand;
                if (handler != null) {
                    // 找到对应的请求处理器，用对应的请求处理器处理 RPC 请求
                    responseCommand = handler.handle(requestCommand);
                } else {
                    // 没有对应的请求处理器，返回无法处理的错误
                    responseCommand = new Command(
                        new ResponseHeader(
                            requestCommand.getHeader().getType(),
                            requestCommand.getHeader().getVersion(),
                            requestCommand.getHeader().getRequestId(),
                            ResultCode.CANNOT_HANDLE.getCode(),
                            "No handler for request with type: " + requestCommand.getHeader().getType()
                        ),
                        new byte[0]
                    );
                }

                if (null != responseCommand) {
                    // 把请求的处理结果发送给 Consumer
                    channelHandlerContext.writeAndFlush(responseCommand).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            // I/O 数据发送失败
                            if (!channelFuture.isSuccess()) {
                                System.out.println("Producer. write response failed! " + channelFuture.cause());
                                channelHandlerContext.channel().close();
                            }
                        }
                    });
                } else {
                    logger.warn("RequestInvocation. Response is null!");
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("RequestInvocation. exception: ", cause);
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }

}