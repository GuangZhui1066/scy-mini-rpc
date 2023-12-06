package com.minirpc.client.stub;

import com.minirpc.transport.Transport;

/**
 * 桩工厂
 *   桩 (stub), 指 Consumer 为 RPC 接口类生成的代理对象
 *   在 Consumer 获取远程服务的时候"动态生成"
 */
public interface StubFactory {

    /**
     * 创建 stub
     */
    <T> T createStub(Transport transport, Class<T> serviceClass);

}
