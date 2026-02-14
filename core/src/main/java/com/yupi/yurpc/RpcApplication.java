package com.yupi.yurpc;

import com.yupi.yurpc.conifg.RegistryConfig;
import com.yupi.yurpc.conifg.RpcConifg;
import com.yupi.yurpc.constant.RpcConstant;
import com.yupi.yurpc.registry.Registry;
import com.yupi.yurpc.registry.RegistryFactory;
import com.yupi.yurpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcApplication {
    private static volatile RpcConifg rpcConfig;
    public static void init(RpcConifg rpcConfig) {
        // 使用自定义传入配置对象的配置
        rpcConfig = rpcConfig;
        log.info("初始化RpcConifg: {}", rpcConfig);
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("初始化Registry: {}", registry);
    }
    public static void init() {
        try {
            // 加载配置文件
            rpcConfig = ConfigUtils.loadConfig(RpcConifg.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            // 使用默认值
            rpcConfig = new RpcConifg();
        }
        log.info("初始化RpcConifg: {}", rpcConfig);
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("初始化Registry: {}", registry);
    }
    /**
     * 双检锁单例模式获取RpcConifg
     *
     * @return
     */
    public static RpcConifg getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
