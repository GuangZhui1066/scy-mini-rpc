package com.minirpc.transport;

import java.io.Closeable;
import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

/**
 * 用于创建 Transport 对象的工厂类
 */
public interface TransportFactory extends Closeable {

    Transport createTransport(SocketAddress address, long connectionTimeout) throws InterruptedException, TimeoutException;

    @Override
    void close();

}
