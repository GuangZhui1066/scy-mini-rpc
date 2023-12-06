package com.minirpc.api.spi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * SPI 加载器
 */
public class ServiceSupport {

    /**
     * 保存单例对象的 Map <className, singletonObject>
     */
    private final static Map<String, Object> singletonObject = new HashMap<>();


    /**
     * 通过 SPI 加载指定类型的所有对象实例
     */
    public synchronized static <T> List<T> loadAll(Class<T> clz) {
        return StreamSupport
            .stream(ServiceLoader.load(clz).spliterator(),false)
            .map(ServiceSupport::getSingletonObj)
            .collect(Collectors.toList());
    }

    public synchronized static <T> T load(Class<T> clz) {
        List<T> objList = loadAll(clz);
        if (objList == null || objList.isEmpty()) {
            throw new RuntimeException("没有找到此类型的对象, class: " + clz.getCanonicalName());
        }
        return objList.get(0);
    }


    /**
     * 如果是单例的类，就返回这个类的单例对象
     */
    private static <T> T getSingletonObj(T obj) {
        if (obj.getClass().isAnnotationPresent(Singleton.class)) {
            String className = obj.getClass().getCanonicalName();
            singletonObject.putIfAbsent(className, obj);
            return (T) singletonObject.get(className);
        } else {
            return obj;
        }
    }

}
