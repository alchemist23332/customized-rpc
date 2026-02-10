package com.yupi.yurpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.RpcResponse;
import com.yupi.yurpc.serializer.JdkSerializer;
import com.yupi.yurpc.serializer.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final Serializer serializer = new JdkSerializer();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args).build();
        try {
            // 序列化
            byte[] serialized = serializer.serialize(rpcRequest);
            // 发送请求,写死请求地址
            try(HttpResponse response = HttpRequest.post("http://localhost:8080").body(serialized).execute()) {
                byte[] bytes = response.bodyBytes();
                return serializer.deserialize(bytes, RpcResponse.class).getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
