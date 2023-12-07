package com.minirpc.consumer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

            // 模拟并发请求
            //
            // 在 Provider 端同步处理的情况下，并发的三个请求就会在 Provider 端排队，被依次处理。
            // 假设每次处理需要2s时间，那么:
            //    第一个请求在发出后 2s 后返回
            //    第二个请求在发出后 4s 后返回
            //    第三个请求在发出后 6s 后返回
            //
            // 在 Provider 端异步处理(线程池)的情况下，并发的三个请求就会在 Provider 端被同时处理。
            // 假设每次处理需要2s时间，那么:
            //    在同时发出三个请求的 2s 后，三个请求都会同时返回

            ExecutorService executorService = Executors.newFixedThreadPool(3);

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    logger.info("RPCConsumer 发出请求1, 时间:{}", sdf.format(new Date()));
                    String rpcResult1 = helloService.hello("scy1");
                    logger.info("RPCConsumer 收到请求1的响应, 时间:{}, 返回值:{}", sdf.format(new Date()), rpcResult1);
                }
            });

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    logger.info("RPCConsumer 发出请求2, 时间:{}", sdf.format(new Date()));
                    String rpcResult2 = helloService.hello("scy2");
                    logger.info("RPCConsumer 收到请求2的响应, 时间:{}, 返回值:{}", sdf.format(new Date()), rpcResult2);
                }
            });

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    logger.info("RPCConsumer 发出请求3, 时间:{}", sdf.format(new Date()));
                    String rpcResult3 = helloService.hello("scy3");
                    logger.info("RPCConsumer 收到请求3的响应, 时间:{}, 返回值:{}", sdf.format(new Date()), rpcResult3);
                }
            });

            executorService.shutdown();

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
            throw new RuntimeException("Consumer. 未找到服务提供者! service: " + serviceName);
        }
        logger.info("Consumer. 找到服务提供者, service:{}, provider:{}", serviceName, uri);

        // 创建 RPC 服务在 Consumer 端的代理对象
        return rpcAccessPoint.getRemoteService(uri, serviceClass);
    }

}
