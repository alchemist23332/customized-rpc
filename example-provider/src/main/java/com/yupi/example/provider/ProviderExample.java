package com.yupi.example.provider;

import com.yupi.example.common.service.UserService;
import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.bootstrap.ProviderBootstrap;
import com.yupi.yurpc.conifg.RegistryConfig;
import com.yupi.yurpc.conifg.RpcConifg;
import com.yupi.yurpc.model.ServiceMetaInfo;
import com.yupi.yurpc.model.ServiceRegisterInfo;
import com.yupi.yurpc.registry.LocalRegistry;
import com.yupi.yurpc.registry.Registry;
import com.yupi.yurpc.registry.RegistryFactory;
import com.yupi.yurpc.server.HttpServer;
import com.yupi.yurpc.server.VertxHttpServer;
import com.yupi.yurpc.server.tcp.VertxTcpServer;

import java.util.ArrayList;
import java.util.List;

public class ProviderExample {
    public static void main(String[] args) {
        List<ServiceRegisterInfo<?>> serviceList = new ArrayList<>();
        serviceList.add(new ServiceRegisterInfo<>(UserService.class.getName(), UserServiceImpl.class));
        ProviderBootstrap.init(serviceList);
    }
}
