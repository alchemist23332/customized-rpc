package com.yupi.example.provider;

import com.yupi.example.common.service.UserService;
import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.conifg.RegistryConfig;
import com.yupi.yurpc.conifg.RpcConifg;
import com.yupi.yurpc.model.ServiceMetaInfo;
import com.yupi.yurpc.registry.LocalRegistry;
import com.yupi.yurpc.registry.Registry;
import com.yupi.yurpc.registry.RegistryFactory;
import com.yupi.yurpc.server.HttpServer;
import com.yupi.yurpc.server.VertxHttpServer;

public class ProviderExample {
    public static void main(String[] args) {
        // Rpc框架初始化
        RpcApplication.init();
        // 本地注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        // 配置服务到注册中心
        RpcConifg rpcConifg = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = RpcApplication.getRpcConfig().getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        //todo 这里写死了服务版本号和服务分组
        ServiceMetaInfo myService = ServiceMetaInfo.builder()
                .serviceName(UserService.class.getName())
                .serviceHost(rpcConifg.getServerHost())
                .servicePort(rpcConifg.getPort())
                .serviceVersion("1.0")
                .serviceGroup("default")
                .build();
        try {
            registry.register(myService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 启动服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getPort());
        System.out.println("启动成功");
    }
}
