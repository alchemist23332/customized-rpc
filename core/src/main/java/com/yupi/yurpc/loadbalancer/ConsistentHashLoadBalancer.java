package com.yupi.yurpc.loadbalancer;

import cn.hutool.core.collection.CollectionUtil;
import com.yupi.yurpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConsistentHashLoadBalancer implements LoadBalancer{
    /**
     * 虚拟hash环(实际treemap底层是红黑树)
     * 这里为了有序，使用TreeMap
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();
    private static final int VIRTUAL_NODE_NUM = 100;
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (CollectionUtil.isEmpty(serviceMetaInfoList)) {
            return null;
        }
        // 构建hash环
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                String virtualNodeName = serviceMetaInfo.getServiceAddress() + "#" + i;
                int hash = getHash(virtualNodeName);
                virtualNodes.put(hash, serviceMetaInfo);
            }
        }
        // 获取请求参数的hash值，并第一个>=此值对应的服务提供者
        int hash = getHash(requestParams);
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);
        if (entry == null) {
            // 如果没有大于等于调用请求 hash 值的虚拟节点，则返回头节点（因为是个环）
            entry = virtualNodes.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * Hash 算法，可自行实现
     *
     * @param key
     * @return
     */
     private int getHash(Object key) {
         return key.hashCode();
     }
}
