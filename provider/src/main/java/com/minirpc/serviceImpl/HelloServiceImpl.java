package com.minirpc.serviceImpl;

import com.minirpc.service.api.HelloService;

/**
 * RPC Provider 提供服务的实现
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        // 测试请求超时
        //try {
        //    Thread.sleep(5 * 1000);
        //} catch (Exception ignored) {}

        return "...Hello, " + name + " !...";
    }

}
