package com.minirpc.provider;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.minirpc.api.NameService;
import com.minirpc.api.RpcAccessPoint;
import com.minirpc.api.spi.ServiceSupport;
import com.minirpc.service.api.HelloService;
import com.minirpc.serviceImpl.HelloServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC 服务提供者
 */
public class Provider {

    private static final Logger logger = LoggerFactory.getLogger(Provider.class);

    /**
     * 本机地址
     */
    private final String host = "localhost";
    private final int port = 9999;
    private final URI uri = URI.create("mini-rpc://" + host + ":" + port);

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

        rpcAccessPoint = ServiceSupport.load(RpcAccessPoint.class);

        // 支持 LocalFileNameService 和 MySQLNameService
        // 可以在 SPI 配置文件 (com.minirpc.api.NameService) 中灵活配置
        nameService = rpcAccessPoint.getNameService(nameServiceDataFile.toURI());
        assert nameService != null;

        // 启动远程 RPC 框架
        try {
            rpcAccessPoint.startServer();
        } catch (Exception e) {
            logger.error("RPC Server start fail.", e);
        }
    }


    /**
     * 测试入口类：注册 RPC 服务 HelloService
     */
    public static void main(String [] args) throws Exception {
        try {
            HelloServiceImpl helloServiceImpl = new HelloServiceImpl();
            // 注册 RPC 服务，类似于 HSF 中的 @HSFProvider 注解的作用
            registerRpcService(HelloService.class, helloServiceImpl);
        } catch (Exception e) {
            logger.error("Provider. register hello rpc service error", e);
        }

        // 按任意键退出
        System.in.read();
        rpcAccessPoint.closeNameService();
        logger.info("Provider. stop service...");
    }


    /**
     * Provider 注册一个 RPC 服务
     */
    private static <T> void registerRpcService(Class<T> serviceClass, T serviceImpl) throws InstantiationException, IllegalAccessException, IOException {
        String serviceName = serviceClass.getCanonicalName();

        // 向 RPC 框架注册服务提供者的实例，返回 provider 的地址
        URI uri = rpcAccessPoint.addServiceProvider(serviceImpl, serviceClass);

        // 向注册中心注册服务提供者 (服务名 + 服务提供者地址)
        nameService.registerService(serviceName, uri);

        logger.info("Provider. 注册 {} 服务", serviceName);
    }

}
