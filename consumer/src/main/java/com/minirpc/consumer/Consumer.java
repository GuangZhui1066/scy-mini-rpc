package com.minirpc.consumer;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.minirpc.api.NameService;
import com.minirpc.api.RpcAccessPoint;
import com.minirpc.api.spi.ServiceSupport;
import com.minirpc.service.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC 服务消费者
 */
public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    /**
     * RPC 框架接口
     */
    private static RpcAccessPoint rpcAccessPoint;

    /**
     * 注册中心
     */
    private static NameService nameService;


    static {
        // 在当前目录下创建 name_service.data 文件，用于存储 RPC 服务对应的 Provider 的地址
        File currentDir = new File(".");
        File nameServiceDataFile = new File(currentDir, "name_service.data");

        // 注：SPI 的实现和配置在 netty-rpc 模块中，需要在 consumer 模块中依赖 netty-rpc 模块
        rpcAccessPoint = ServiceSupport.load(RpcAccessPoint.class);
        nameService = rpcAccessPoint.getNameService(nameServiceDataFile.toURI());
        assert nameService != null;
    }


    /**
     * 测试入口类：调用 RPC 服务 HelloService
     */
    public static void main(String [] args) {
        HelloService helloService;
        try {
            // 获取 RPC 接口的实现类，类似于 HSF 中的 @HSFConsumer 注解的作用
            helloService = getRpcConsumerService(HelloService.class);
        } catch (Exception e) {
            logger.info("RPCConsumer 获取服务异常", e);
            return;
        }

        // 调用 RPC 服务
        try {
            String rpcResult = helloService.hello("scy");
            logger.info("RPCConsumer 收到请求响应, rpcResult: " + rpcResult);
        } catch (Exception e) {
            logger.error("RPCConsumer 请求异常", e);
        }
    }


    /**
     * 获取 RPC 接口在 Consumer 端的代理对象 (桩)
     */
    private static <T> T getRpcConsumerService(Class<T> serviceClass) throws IOException {
        String serviceName = serviceClass.getCanonicalName();

        // 通过注册中心查找服务提供者的地址
        URI uri = nameService.lookupService(serviceName);
        if (uri == null) {
            throw new RuntimeException("RPCConsumer 未找到服务提供者! service: " + serviceName);
        }
        logger.info("RPCConsumer 找到服务提供者, service:{}, provider:{}", serviceName, uri);

        // 创建 RPC 服务在 Consumer 端的代理对象
        return rpcAccessPoint.getRemoteService(uri, serviceClass);
    }

}
