package com.yupi.yurpc.registry;

import com.yupi.yurpc.conifg.RegistryConfig;
import com.yupi.yurpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * 注册中心统一接口
 *
 */
public interface Registry {
    /**
     * 初始化
     */
    void init(RegistryConfig registryConfig);
    /**
     * 注册服务（服务提供端）
     *
     * @param serviceMetaInfo 服务元信息
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;
    /**
     * 服务发现（服务消费端）
     *
     * @param serviceKey 服务键
     * @return 服务元信息
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey) throws Exception;

    /**
     * 服务注销（服务提供端）
     *
     * @param serviceMetaInfo 服务元信息
     */
    void unRegister(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 注册中心销毁
     */
    void destroy();

    /**
     * 心跳检测（服务端）
     */
    void heartBeat();

    /**
     * 监听（消费端）
     *
     * @param serviceNodeKey
     */
    void watch(String serviceNodeKey);

}
