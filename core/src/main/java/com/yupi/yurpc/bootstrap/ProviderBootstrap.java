package com.yupi.yurpc.bootstrap;

import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.conifg.RegistryConfig;
import com.yupi.yurpc.conifg.RpcConifg;
import com.yupi.yurpc.model.ServiceMetaInfo;
import com.yupi.yurpc.model.ServiceRegisterInfo;
import com.yupi.yurpc.registry.LocalRegistry;
import com.yupi.yurpc.registry.Registry;
import com.yupi.yurpc.registry.RegistryFactory;
import com.yupi.yurpc.server.tcp.VertxTcpClient;
import com.yupi.yurpc.server.tcp.VertxTcpServer;

import java.util.List;

public class ProviderBootstrap {
    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {
        // Rpc框架初始化
        RpcApplication.init();
        // 全局配置
        RpcConifg rpcConfig = RpcApplication.getRpcConfig();
        // 本地注册服务 + 配置服务到注册中心
        serviceRegisterInfoList.forEach(serviceRegisterInfo -> {
            String serviceName = serviceRegisterInfo.getServiceName();
            Class<?> implClass = serviceRegisterInfo.getImplClass();
            // 1.本地注册
            LocalRegistry.register(serviceName, implClass);
            // 2.配置服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            ServiceMetaInfo myService = ServiceMetaInfo.builder()
                    .serviceName(serviceName)
                    .serviceHost(rpcConfig.getServerHost())
                    .servicePort(rpcConfig.getServerPort())
                    .serviceVersion("1.0")
                    .serviceGroup("default")
                    .build();
            try {
                Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
                registry.register(myService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());
    }
}
