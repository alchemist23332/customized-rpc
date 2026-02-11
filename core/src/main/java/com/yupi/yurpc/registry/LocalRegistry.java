package com.yupi.yurpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalRegistry {
    private static final Map<String, Class<?>> REGISTRY = new ConcurrentHashMap<>();

    /**
     * 注册服务
     *
     * @param serviceName 服务名称
     * @param implClass 实现类(任意类型)
     */
    public static void register(String serviceName, Class<?> implClass) {
        REGISTRY.put(serviceName, implClass);
    }
    /**
     * 获取服务
     *
     * @param serviceName 服务名称
     * @return 服务实现类
     */
    public static Class<?> get(String serviceName) {
        return REGISTRY.get(serviceName);
    }
    /**
     * 删除服务
     *
     * @return
     */
    public static void remove(String serviceName) {
        REGISTRY.remove(serviceName);
    }


}
