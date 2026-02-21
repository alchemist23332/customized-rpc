package com.yupi.yurpc.proxy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.conifg.RegistryConfig;
import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.RpcResponse;
import com.yupi.yurpc.model.ServiceMetaInfo;
import com.yupi.yurpc.protocol.*;
import com.yupi.yurpc.registry.Registry;
import com.yupi.yurpc.registry.RegistryFactory;
import com.yupi.yurpc.serializer.JdkSerializer;
import com.yupi.yurpc.serializer.JsonSerializer;
import com.yupi.yurpc.serializer.Serializer;
import com.yupi.yurpc.serializer.SerializerFactory;
import com.yupi.yurpc.server.tcp.VertxTcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args).build();
        try {
            // 序列化
            byte[] serialized = serializer.serialize(rpcRequest);
            // 获取服务提供者的地址
            // todo 这里写死了服务的版本为默认1.0
            RegistryConfig registryConfig = RpcApplication.getRpcConfig().getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo keyCompose = ServiceMetaInfo.builder()
                    .serviceName(rpcRequest.getServiceName())
                    .serviceVersion("1.0")
                    .build();
            List<ServiceMetaInfo> serviceList = registry.serviceDiscovery(keyCompose.getServiceKey());
            if (CollectionUtil.isEmpty(serviceList)) {
                throw new RuntimeException("服务不存在");
            }
            // todo 负载均衡
            ServiceMetaInfo serviceMetaInfo = serviceList.get(0);
//            // 原方案（http,客户端直接采用hutool）
//            // 发送请求(采用HttpRequest直接发送)
//            try(HttpResponse response = HttpRequest.post(serviceMetaInfo.getServiceAddress()).body(serialized).execute()) {
//                byte[] bytes = response.bodyBytes();
//                return serializer.deserialize(bytes, RpcResponse.class).getData();
//            }


            // 新方案（tcp）采用vertx的客户端
            RpcResponse rpcResponse = VertxTcpClient.doRequest(rpcRequest, serviceMetaInfo);
            return rpcResponse.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
