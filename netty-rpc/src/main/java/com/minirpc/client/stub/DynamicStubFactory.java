package com.minirpc.client.stub;

import com.minirpc.client.stub.cglib.CGLibDynamicProxy;
import com.minirpc.transport.Transport;

/**
 * 动态代理生成桩
 */
public class DynamicStubFactory implements StubFactory {

    @Override
    public <T> T createStub(Transport transport, Class<T> serviceClass) {
        try {
            // CGLib 动态代理生成代理类
            CGLibDynamicProxy proxy = new CGLibDynamicProxy(serviceClass);
            proxy.setTransport(transport);
            // 返回这个桩
            return (T) proxy.getProxy();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
