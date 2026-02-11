package com.yupi.example.consumer;

import com.yupi.yurpc.conifg.RpcConifg;
import com.yupi.yurpc.constant.RpcConstant;
import com.yupi.yurpc.utils.ConfigUtils;

public class ConsumerExample {
    public static void main(String[] args) {
        RpcConifg rpcConifg = ConfigUtils.loadConfig(RpcConifg.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        System.out.println(rpcConifg);
    }
}
