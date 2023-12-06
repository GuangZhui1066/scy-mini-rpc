package com.minirpc.transport.netty.handler.coder.encode;

import java.nio.charset.StandardCharsets;

import com.minirpc.transport.command.Header;
import com.minirpc.transport.command.ResponseHeader;
import com.minirpc.transport.netty.handler.coder.CommandEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 编码 RPC 返回的 Command 对象
 */
public class ResponseEncoder extends CommandEncoder {

    @Override
    protected void encodeHeader(ChannelHandlerContext channelHandlerContext, Header header, ByteBuf byteBuf) throws Exception {
        super.encodeRequestHeader(channelHandlerContext, header, byteBuf);

        if (header instanceof ResponseHeader) {
            ResponseHeader responseHeader = (ResponseHeader) header;
            byteBuf.writeInt(responseHeader.getCode());
            int errorLength = header.encodedLength() - ResponseHeader.fixedLength();
            byteBuf.writeInt(errorLength);
            byteBuf.writeBytes(responseHeader.getErrMsg() == null ?
                new byte[0] :
                responseHeader.getErrMsg().getBytes(StandardCharsets.UTF_8));
        } else {
            throw new Exception(String.format("Invalid header type: %s!", header.getClass().getCanonicalName()));
        }
    }

}
