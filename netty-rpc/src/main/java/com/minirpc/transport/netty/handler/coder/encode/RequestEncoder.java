package com.minirpc.transport.netty.handler.coder.encode;

import com.minirpc.transport.command.Header;
import com.minirpc.transport.netty.handler.coder.CommandEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 编码 RPC 请求的 Command 对象
 */
public class RequestEncoder extends CommandEncoder {

    @Override
    protected void encodeHeader(ChannelHandlerContext channelHandlerContext, Header header, ByteBuf byteBuf) throws Exception {
        super.encodeBaseHeader(channelHandlerContext, header, byteBuf);
    }

}
