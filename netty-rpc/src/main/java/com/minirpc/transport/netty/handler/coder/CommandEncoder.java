package com.minirpc.transport.netty.handler.coder;

import com.minirpc.transport.command.Command;
import com.minirpc.transport.command.Header;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码 Command 对象
 *
 * 编码格式：
 *     总字节数     +      请求头      +         请求体
 *      1字节       3字节 / 5字节+errMsg      payload长度
 */
public abstract class CommandEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if(!(o instanceof Command)) {
            throw new Exception(String.format("CommandEncoder. unknown type: %s!", o.getClass().getCanonicalName()));
        }

        Command command = (Command) o;
        int commandLength = Integer.BYTES + command.getHeader().encodedLength() + command.getPayload().length;

        // 总字节数
        byteBuf.writeInt(commandLength);

        // 请求头/响应头
        encodeHeader(channelHandlerContext, command.getHeader(), byteBuf);

        // 请求体
        byteBuf.writeBytes(command.getPayload());
    }

    /**
     * 编码 请求头
     */
    protected abstract void encodeHeader(ChannelHandlerContext channelHandlerContext, Header header, ByteBuf byteBuf) throws Exception;

    protected void encodeBaseHeader(ChannelHandlerContext channelHandlerContext, Header header, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt(header.getType());
        byteBuf.writeInt(header.getVersion());
        byteBuf.writeInt(header.getRequestId());
    }

}
