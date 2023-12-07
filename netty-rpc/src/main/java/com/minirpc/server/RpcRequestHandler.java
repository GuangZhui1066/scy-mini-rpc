package com.minirpc.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.minirpc.api.spi.Singleton;
import com.minirpc.serialize.SerializeSupport;
import com.minirpc.transport.command.Command;
import com.minirpc.transport.command.Header;
import com.minirpc.transport.command.ResponseHeader;
import com.minirpc.transport.command.ResultCode;
import com.minirpc.transport.command.request.RequestType;
import com.minirpc.transport.command.request.RpcRequest;

/**
 * Provider 处理 RPC 请求的处理器
 *   Provider 接收 Consumer 发出的 RPC 请求，处理并返回
 */
@Singleton
public class RpcRequestHandler implements RequestHandler, ServiceProviderRegistry {

    /**
     * 保存 <serviceName, service实现类实例>
     */
    private Map<String, Object> serviceProviders = new HashMap<>();


    /**
     * Provider 端的核心逻辑
     */
    @Override
    public Command handle(Command requestCommand) {
        Header header = requestCommand.getHeader();
        // 反序列化 RpcRequest
        RpcRequest rpcRequest = SerializeSupport.parse(requestCommand.getPayload());
        try {
            // 查找已注册的 provider (服务实现类) 实例，寻找 rpcRequest 中需要的服务
            Object serviceProvider = serviceProviders.get(rpcRequest.getInterfaceName());
            if (serviceProvider == null) {
                // 没找到 provider
                System.out.println("No service Provider of " + rpcRequest.getInterfaceName() + "#" + rpcRequest.getMethodName());
                return new Command(new ResponseHeader(type(), header.getVersion(), header.getRequestId(), ResultCode.NO_PROVIDER.getCode(), ResultCode.NO_PROVIDER.getMessage()), new byte[0]);
            }
            // 找到 provider，反射调用
            String arg = SerializeSupport.parse(rpcRequest.getSerializedArguments());
            Method method = serviceProvider.getClass().getMethod(rpcRequest.getMethodName(), String.class);
            String result = (String) method.invoke(serviceProvider, arg);
            // 将结果序列化，封装成 Command 返回
            ResponseHeader responseHeader = new ResponseHeader(type(), header.getVersion(), header.getRequestId());
            byte [] payload = SerializeSupport.serialize(result);
            return new Command(responseHeader, payload);

        } catch (Throwable t) {
            System.out.println("handle request Exception: " + t.getMessage());
            return new Command(new ResponseHeader(type(), header.getVersion(), header.getRequestId(), ResultCode.UNKNOWN_ERROR.getCode(), t.getMessage()), new byte[0]);
        }
    }

    @Override
    public int type() {
        return RequestType.RPC_REQUEST.getCode();
    }

    @Override
    public <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider) {
        serviceProviders.put(serviceClass.getCanonicalName(), serviceProvider);
        System.out.println("Service provider registered"
            + ". service: " + serviceClass.getCanonicalName()
            + ", provider: " + serviceProvider.getClass().getCanonicalName());
    }

}
