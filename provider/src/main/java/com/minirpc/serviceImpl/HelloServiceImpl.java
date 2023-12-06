package com.minirpc.serviceImpl;

import com.minirpc.service.api.HelloService;

/**
 * RPC Provider 提供服务的实现
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello, " + name + " !";
    }

}
