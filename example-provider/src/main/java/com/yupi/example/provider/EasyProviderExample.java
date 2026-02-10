package com.yupi.example.provider;

import com.yupi.example.common.service.UserService;
import com.yupi.yurpc.registry.LocalRegistry;
import com.yupi.yurpc.server.HttpServer;
import com.yupi.yurpc.server.VertxHttpServer;

public class EasyProviderExample {
    public static void main(String[] args) {
        // 注册服务到本地注册 全类名com.yupi.example.common.service.UserService作为key, 实现类的字节码对象作为value
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        // 启动 HTTP 服务器
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
