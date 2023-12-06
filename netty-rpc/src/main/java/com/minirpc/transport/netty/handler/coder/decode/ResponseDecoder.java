package com.minirpc.transport.netty.handler.coder.decode;

import java.nio.charset.StandardCharsets;

import com.minirpc.transport.command.Header;
import com.minirpc.transport.command.ResponseHeader;
import com.minirpc.transport.netty.handler.coder.CommandDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 解码 RPC 请求返回的 Command 对象
 */
public class ResponseDecoder extends CommandDecoder {

    @Override
    protected Header decodeHeader(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        int type = byteBuf.readInt();
        int version = byteBuf.readInt();
        int requestId = byteBuf.readInt();

        int code = byteBuf.readInt();
        int errorLength = byteBuf.readInt();
        byte [] errorBytes = new byte[errorLength];
        byteBuf.readBytes(errorBytes);
        String error = new String(errorBytes, StandardCharsets.UTF_8);

        return new ResponseHeader(type, version, requestId, code, error);
    }

}
