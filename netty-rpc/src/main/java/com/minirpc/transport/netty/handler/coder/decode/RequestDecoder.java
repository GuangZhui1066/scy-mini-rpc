package com.minirpc.transport.netty.handler.coder.decode;

import com.minirpc.transport.command.Header;
import com.minirpc.transport.netty.handler.coder.CommandDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 解码 RPC 请求的 Command 对象
 */
public class RequestDecoder extends CommandDecoder {

    @Override
    protected Header decodeHeader(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        return new Header(
            byteBuf.readInt(),
            byteBuf.readInt(),
            byteBuf.readInt()
        );
    }

}
