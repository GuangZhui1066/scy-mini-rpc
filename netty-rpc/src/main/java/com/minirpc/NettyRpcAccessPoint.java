package com.minirpc;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import com.minirpc.api.NameService;
import com.minirpc.api.RpcAccessPoint;
import com.minirpc.api.spi.ServiceSupport;
import com.minirpc.client.stub.StubFactory;
import com.minirpc.server.RequestHandlerRegistry;
import com.minirpc.server.ServiceProviderRegistry;
import com.minirpc.transport.Transport;
import com.minirpc.transport.TransportFactory;
import com.minirpc.transport.TransportServer;

public class NettyRpcAccessPoint implements RpcAccessPoint {

    /**
     * provider 提供服务的端口
     */
    private final int port = 9999;

    private NameService nameService = null;

    private TransportServer transportServer = null;

    private TransportFactory transportFactory = ServiceSupport.load(TransportFactory.class);

    private final StubFactory stubFactory = ServiceSupport.load(StubFactory.class);

    private final ServiceProviderRegistry serviceProviderRegistry = ServiceSupport.load(ServiceProviderRegistry.class);

    /**
     * 保存每个 URI 和对应的 Transport 对象，可以通过 Transport 对象与 URI 进行网络通信
     */
    private final Map<URI, Transport> transportMap = new ConcurrentHashMap<>();


    @Override
    public <T> URI addServiceProvider(T service, Class<T> serviceClass) {
        serviceProviderRegistry.addServiceProvider(serviceClass, service);
        return getProviderUri();
    }

    /**
     * 获取 provider 的地址
     */
    private URI getProviderUri() {
        return URI.create("mini-rpc://localhost:" + port);
    }

    @Override
    public <T> T getRemoteService(URI uri, Class<T> serviceClass) {
        // 获取或创建这个 URI 对应的连接对象，保存
        Transport transport = transportMap.computeIfAbsent(uri, this::createTransport);
        // 在运行时动态地生成桩
        T serviceStub = stubFactory.createStub(transport, serviceClass);
        return serviceStub;
    }

    private Transport createTransport(URI uri) {
        try {
            return transportFactory.createTransport(
                new InetSocketAddress(uri.getHost(), uri.getPort()), 30000L
            );
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Closeable startServer() throws Exception {
        if (null == transportServer) {
            transportServer = ServiceSupport.load(TransportServer.class);
            transportServer.start(RequestHandlerRegistry.getInstance(), port);
        }
        return new Closeable() {
            @Override
            public void close() throws IOException {
                if (null != transportServer) {
                    transportServer.stop();
                }
            }
        };
    }

    @Override
    public NameService getNameService(URI nameServiceUri) {
        nameService = ServiceSupport.load(NameService.class);
        nameService.connect(nameServiceUri);
        return nameService;

    }

    @Override
    public void closeNameService() {
        try {
            nameService.close();
        } catch (Exception ignored) {}
    }

    @Override
    public void close() {
        if(null != transportServer) {
            transportServer.stop();
        }
        transportFactory.close();
    }

}
