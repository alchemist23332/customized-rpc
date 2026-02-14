package com.yupi.yurpc.server;

import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.RpcResponse;
import com.yupi.yurpc.registry.LocalRegistry;
import com.yupi.yurpc.serializer.JdkSerializer;
import com.yupi.yurpc.serializer.JsonSerializer;
import com.yupi.yurpc.serializer.Serializer;
import com.yupi.yurpc.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.lang.reflect.Method;

public class HttpServerHandler implements Handler<HttpServerRequest> {
    /*1. 反序列化请求为对象，并从请求对象中获取参数。
    2. 根据服务名称从本地注册器中获取到对应的服务实现类。
    3. 通过反射机制调用方法，得到返回结果。
    4. 对返回结果进行封装和序列化，并写入到响应中。*/
    @Override
    public void handle(HttpServerRequest request) {
        //指定序列化方式
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        // 记录日志
        System.out.println("Received request: " + request.method() + " " + request.uri());
        // 处理请求体

        request.bodyHandler(body -> {
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(body.getBytes(), RpcRequest.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            RpcResponse rpcResponse = new RpcResponse();
            // 请求参数校验
            if (rpcRequest == null) {
                rpcResponse.setMessage("请求内容为空");
                doResponse(request, rpcResponse, serializer);
                return;
            }
            // 获取服务
            try {
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("success");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            doResponse(request, rpcResponse, serializer);
        });
    }
    /**
     * 响应处理
     * @param request
     * @param rpcResponse
     * @param serializer
     */
    void doResponse(HttpServerRequest request,RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse response = request.response().putHeader("Content-Type", "application/json");
        try {
            byte[] serialized = serializer.serialize(rpcResponse);
            //Buffer支持直接内存（Direct Memory）。在网络传输时，数据可以直接从内核空间拷贝到网卡，跳过 JVM 堆内存。
            response.end(Buffer.buffer(serialized));
        } catch (Exception e) {
            e.printStackTrace();
            response.end(Buffer.buffer());
        }
    }
}
