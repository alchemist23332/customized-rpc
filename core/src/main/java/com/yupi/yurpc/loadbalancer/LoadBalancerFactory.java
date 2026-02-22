package com.yupi.yurpc.loadbalancer;

import com.yupi.yurpc.spi.SpiLoader;

public class LoadBalancerFactory {
    /**
     * 静态加载
     */
    static {
        SpiLoader.load(LoadBalancer.class);
    }
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RoundRobinLoadBalancer();
    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static LoadBalancer getInstance(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }
}
