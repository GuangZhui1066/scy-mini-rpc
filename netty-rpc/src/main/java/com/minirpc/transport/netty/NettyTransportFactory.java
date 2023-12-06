package com.minirpc.transport.netty;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.minirpc.transport.InFlightRequests;
import com.minirpc.transport.Transport;
import com.minirpc.transport.TransportFactory;
import com.minirpc.transport.netty.handler.ResponseInvocation;
import com.minirpc.transport.netty.handler.coder.decode.ResponseDecoder;
import com.minirpc.transport.netty.handler.coder.encode.RequestEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 用 Netty 创建网络通信对象
 */
public class NettyTransportFactory implements TransportFactory {

    private EventLoopGroup ioEventGroup;

    private Bootstrap bootstrap;

    private final InFlightRequests inFlightRequests;

    private List<Channel> channels = new LinkedList<>();


    public NettyTransportFactory() {
        this.inFlightRequests = new InFlightRequests();
    }


    @Override
    public Transport createTransport(SocketAddress address, long connectionTimeout) throws InterruptedException, TimeoutException {
        return new NettyTransport(
            createChannel(address, connectionTimeout),
            inFlightRequests
        );
    }

    private synchronized Channel createChannel(SocketAddress address, long connectionTimeout) throws InterruptedException, TimeoutException {
        if (address == null) {
            throw new IllegalArgumentException("address must not be null!");
        }
        if (ioEventGroup == null) {
            ioEventGroup = newIoEventGroup();
        }
        if (bootstrap == null){
            ChannelHandler channelHandlerPipeline = newChannelHandlerPipeline();
            bootstrap = newBootstrap(channelHandlerPipeline, ioEventGroup);
        }

        ChannelFuture channelFuture = bootstrap.connect(address);
        if (!channelFuture.await(connectionTimeout)) {
            throw new TimeoutException();
        }
        Channel channel = channelFuture.channel();
        if (channel == null || !channel.isActive()) {
            throw new IllegalStateException();
        }
        channels.add(channel);
        return channel;
    }

    private EventLoopGroup newIoEventGroup() {
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup();
        } else {
            return new NioEventLoopGroup();
        }
    }

    /**
     * Consumer 端的 I/O 事件处理器
     */
    private ChannelHandler newChannelHandlerPipeline() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline()
                    // 解码 RPC 返回的 Command
                    .addLast(new ResponseDecoder())
                    // 编码 RPC 请求的 Command
                    .addLast(new RequestEncoder())
                    // 处理收到响应的 RPC 请求
                    .addLast(new ResponseInvocation(inFlightRequests));
            }
        };
    }

    private Bootstrap newBootstrap(ChannelHandler channelHandler, EventLoopGroup ioEventGroup) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
            .group(ioEventGroup)
            .handler(channelHandler)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        return bootstrap;
    }


    @Override
    public void close() {
        for (Channel channel : channels) {
            if(null != channel) {
                channel.close();
            }
        }
        if (ioEventGroup != null) {
            ioEventGroup.shutdownGracefully();
        }
        inFlightRequests.close();
    }

}
