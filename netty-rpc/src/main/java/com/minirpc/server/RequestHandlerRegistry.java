package com.minirpc.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.minirpc.api.spi.ServiceSupport;

/**
 * Provider 的请求处理器注册表
 *   保存 <请求类型, 对应的请求处理器>
 */
public class RequestHandlerRegistry {

    private Map<Integer, RequestHandler> handlerMap = new HashMap<>();

    /**
     * 单例模式
     */
    private static RequestHandlerRegistry instance = null;

    public static RequestHandlerRegistry getInstance() {
        if (null == instance) {
            instance = new RequestHandlerRegistry();
        }
        return instance;
    }

    private RequestHandlerRegistry() {
        Collection<RequestHandler> requestHandlers = ServiceSupport.loadAll(RequestHandler.class);
        for (RequestHandler requestHandler : requestHandlers) {
            handlerMap.put(requestHandler.type(), requestHandler);
            System.out.println("Load request handler, type: " + requestHandler.type()
                + ", class: " + requestHandler.getClass().getCanonicalName());
        }
    }


    public RequestHandler get(int type) {
        return handlerMap.get(type);
    }

}
