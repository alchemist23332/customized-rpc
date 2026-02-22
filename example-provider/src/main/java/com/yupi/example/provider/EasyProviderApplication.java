package com.yupi.example.provider;

import com.yupi.example.common.service.UserService;
import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.registry.LocalRegistry;
import com.yupi.yurpc.serializer.Serializer;
import com.yupi.yurpc.serializer.SerializerFactory;
import com.yupi.yurpc.server.HttpServer;
import com.yupi.yurpc.server.VertxHttpServer;
import com.yupi.yurpc.spi.SpiLoader;

import java.util.Map;
import java.util.ServiceLoader;

public class EasyProviderApplication {
    public static void main(String[] args) {
        // Rpc框架初始化
        RpcApplication.init();
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
//        SpiLoader.loadAll();
//        Map<String, Map<String, Class<?>>> allSpi = SpiLoader.getAllSpi();
//        System.out.println(allSpi);
        System.out.println("启动成功");

    }
}
