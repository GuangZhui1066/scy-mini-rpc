package com.minirpc.client.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * requestId 生成器
 *   生成唯一、递增的请求id
 */
public class RequestIdUtil {

    private static final AtomicInteger NEXT_REQUEST_ID = new AtomicInteger(101);

    public static int next() {
        return NEXT_REQUEST_ID.getAndIncrement();
    }

}
