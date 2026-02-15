package com.yupi.yurpc.registry;

import com.yupi.yurpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryServiceMultiCache {
    /**
     * 服务缓存   服务接口名 ： 服务实例元信息列表
     */
    Map<String, List<ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();
    /**
     * 写缓存
     *
     * @param serviceKey 服务键名
     * @param newServiceCache 更新后的缓存列表
     * @return
     */
    public void writeCache(String serviceKey, List<ServiceMetaInfo> newServiceCache) {
        serviceCache.put(serviceKey, newServiceCache);
    }
    /**
     * 读缓存
     *
     * @param serviceKey 服务键名
     * @return
     */
    public List<ServiceMetaInfo> readCache(String serviceKey) {
        return serviceCache.get(serviceKey);
    }
    /**
     * 删除缓存
     *
     * @param serviceKey 服务键名
     */
    public void deleteCache(String serviceKey) {
        serviceCache.remove(serviceKey);
    }
}
