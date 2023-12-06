package com.minirpc.client.stub.cglib;

import java.lang.reflect.Method;

import com.minirpc.client.stub.AbstractStub;
import com.minirpc.serialize.SerializeSupport;
import com.minirpc.transport.command.request.RpcRequest;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CGLibDynamicProxy extends AbstractStub implements MethodInterceptor {

    private final Class clz;

    public CGLibDynamicProxy(Class clz){
        this.clz = clz;
    }

    public Object getProxy(){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clz);
        enhancer.setCallback(this);
        return enhancer.create();
    }


    /**
     * Consumer 调用 RPC 服务时执行此代理方法
     */
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        // 返回结果是字符数组，需要进行反序列化
        return SerializeSupport.parse(
            // 远程调用 Provider
            // todo 这里默认只有一个参数的情况
            invokeRpcRemote(new RpcRequest(clz.getCanonicalName(), method.getName(), SerializeSupport.serialize(objects[0])))
        );
    }

}
