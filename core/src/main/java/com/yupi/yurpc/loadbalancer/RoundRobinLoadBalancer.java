package com.yupi.yurpc.loadbalancer;

import cn.hutool.core.collection.CollectionUtil;
import com.yupi.yurpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer{
    /**
     * 轮询下标计数器
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        // 判断服务提供者列表是否为空
        if (CollectionUtil.isEmpty(serviceMetaInfoList)) {
            return null;
        }
        // 如果只有一个服务提供者，则直接返回
        int size = serviceMetaInfoList.size();
        if (size == 1) {
            return serviceMetaInfoList.get(0);
        }
        // 轮询
        int index = currentIndex.getAndIncrement() % size;
        return serviceMetaInfoList.get(index);
    }
}
