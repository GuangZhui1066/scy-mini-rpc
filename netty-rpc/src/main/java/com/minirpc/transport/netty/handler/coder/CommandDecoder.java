package com.minirpc.transport.netty.handler.coder;

import java.util.List;

import com.minirpc.transport.command.Command;
import com.minirpc.transport.command.Header;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 解码 Command 对象
 */
public abstract class CommandDecoder extends ByteToMessageDecoder {

    private static final int LENGTH_FIELD_LENGTH = Integer.BYTES;


    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        if (!byteBuf.isReadable(LENGTH_FIELD_LENGTH)) {
            return;
        }
        byteBuf.markReaderIndex();

        int length = byteBuf.readInt() - LENGTH_FIELD_LENGTH;
        if (byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex();
            return;
        }

        Header header = decodeHeader(channelHandlerContext, byteBuf);
        int payloadLength  = length - header.encodedLength();
        byte [] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);

        list.add(new Command(header, payload));
    }

    protected abstract Header decodeHeader(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) ;

}
