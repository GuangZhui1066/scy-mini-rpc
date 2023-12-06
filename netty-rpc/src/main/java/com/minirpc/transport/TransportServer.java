package com.minirpc.transport;

import com.minirpc.server.RequestHandlerRegistry;

public interface TransportServer {

    void start(RequestHandlerRegistry requestHandlerRegistry, int port) throws Exception;

    void stop();

}
