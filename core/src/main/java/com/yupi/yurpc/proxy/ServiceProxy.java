package com.yupi.yurpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.conifg.RegistryConfig;
import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.RpcResponse;
import com.yupi.yurpc.model.ServiceMetaInfo;
import com.yupi.yurpc.registry.Registry;
import com.yupi.yurpc.registry.RegistryFactory;
import com.yupi.yurpc.serializer.JdkSerializer;
import com.yupi.yurpc.serializer.JsonSerializer;
import com.yupi.yurpc.serializer.Serializer;
import com.yupi.yurpc.serializer.SerializerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

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
            RegistryConfig registryConfig = RpcApplication.getRpcConfig().getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo keyCompose = ServiceMetaInfo.builder()
                    .serviceName(rpcRequest.getServiceName())
                    .serviceVersion(rpcRequest.getServiceVersion())
                    .build();
            List<ServiceMetaInfo> serviceList = registry.serviceDiscovery(keyCompose.getServiceKey());
            // todo 负载均衡
            ServiceMetaInfo serviceMetaInfo = serviceList.get(0);
            // 发送请求
            try(HttpResponse response = HttpRequest.post(serviceMetaInfo.getServiceAddress()).body(serialized).execute()) {
                byte[] bytes = response.bodyBytes();
                return serializer.deserialize(bytes, RpcResponse.class).getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
