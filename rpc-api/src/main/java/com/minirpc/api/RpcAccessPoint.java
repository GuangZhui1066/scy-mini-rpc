package com.minirpc.api;

import java.io.Closeable;
import java.net.URI;

/**
 * RPC 框架对外提供的服务接口
 */
public interface RpcAccessPoint extends Closeable {

    /**
     * 服务端调用，注册服务的实现实例
     *
     * @param service 实现实例
     * @param serviceClass 服务的接口类的Class
     * @param <T> 服务接口的类型
     * @return 服务地址
     */
    <T> URI addServiceProvider(T service, Class<T> serviceClass);

    /**
     * 客户端调用，获取远程服务的引用
     *
     * @param uri 远程服务地址
     * @param serviceClass 服务的接口类的Class
     * @param <T> 服务接口的类型
     * @return 远程服务引用
     */
    <T> T getRemoteService(URI uri, Class<T> serviceClass);

    /**
     * 服务端启动RPC框架，监听接口，开始提供远程服务
     *
     * @return 服务实例
     */
    Closeable startServer() throws Exception;

}