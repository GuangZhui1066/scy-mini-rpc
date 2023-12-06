package com.minirpc.transport;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 保存 RPC 消费者已经发出但还没有收到响应的 RPC 请求
 */
public class InFlightRequests implements Closeable {

    private final static long TIMEOUT_SEC = 3L;

    /**
     * 信号量，控制并发访问的数量。
     *   大小为 1 的信号量可以作为互斥锁，并发时只能有一个线程获取；
     *   大小为 n 的信号量可以实现限流，并发时只能有 n 个线程同时获取到信号量
     *
     * 这里设置同时最多只能有 10 个在途请求，避免服务端处理的请求积压
     */
    private final Semaphore semaphore = new Semaphore(10);

    /**
     * 保存已经发出了请求但是还没有收到响应的 ResponseFuture 对象
     */
    private final Map<Integer, ResponseFuture> futureMap = new ConcurrentHashMap<>();

    /**
     * 定时执行的周期性任务，用于定时清除超时的请求
     */
    private final ScheduledFuture scheduledFuture;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();


    public InFlightRequests() {
        // 定义定时任务，每3秒执行一次
        this.scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this::removeTimeoutFutures, TIMEOUT_SEC, TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    /**
     * 清除超时的请求 (超过3秒没有返回的请求即为超时请求)
     */
    private void removeTimeoutFutures() {
        futureMap.entrySet().removeIf(entry -> {
            if (System.nanoTime() - entry.getValue().getTimestamp() > TIMEOUT_SEC * 1000000000L) {
                semaphore.release();
                return true;
            } else {
            return false;
            }
        });
    }


    public void put(ResponseFuture responseFuture) throws InterruptedException, TimeoutException {
        // 获取一个许可
        if(semaphore.tryAcquire(TIMEOUT_SEC, TimeUnit.SECONDS)) {
            futureMap.put(responseFuture.getRequestId(), responseFuture);
        } else {
            throw new TimeoutException();
        }
    }

    public ResponseFuture remove(int requestId) {
        ResponseFuture future = futureMap.remove(requestId);
        if (null != future) {
            // 释放一个许可
            semaphore.release();
        }
        return future;
    }

    @Override
    public void close() {
        scheduledFuture.cancel(true);
        scheduledExecutorService.shutdown();
    }

}
