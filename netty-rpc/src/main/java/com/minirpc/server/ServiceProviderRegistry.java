package com.minirpc.server;

/**
 * RPC Provider 注册表
 *   在 Provider 端注册保存 RPC服务接口 和 对应的服务实现类的实例
 */
public interface ServiceProviderRegistry {

    <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider);

}
