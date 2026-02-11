package com.yupi.yurpc;

import com.yupi.yurpc.conifg.RpcConifg;
import com.yupi.yurpc.constant.RpcConstant;
import com.yupi.yurpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcApplication {
    private static volatile RpcConifg rpcConifg;
    public static void init(RpcConifg newConifg) {
        // 使用自定义传入配置对象的配置
        rpcConifg = newConifg;
        log.info("初始化RpcConifg: {}", rpcConifg);
    }
    public static void init() {
        try {
            // 加载配置文件
            rpcConifg = ConfigUtils.loadConfig(RpcConifg.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            // 使用默认值
            rpcConifg = new RpcConifg();
        }
        log.info("初始化RpcConifg: {}", rpcConifg);
    }
    /**
     * 双检锁单例模式获取RpcConifg
     *
     * @return
     */
    public static RpcConifg getRpcConifg() {
        if (rpcConifg == null) {
            synchronized (RpcApplication.class) {
                if (rpcConifg == null) {
                    init();
                }
            }
        }
        return rpcConifg;
    }
}
