package com.yupi.yurpc.server.tcp;

import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.RpcResponse;
import com.yupi.yurpc.protocol.ProtocolMessage;
import com.yupi.yurpc.protocol.ProtocolMessageDecoder;
import com.yupi.yurpc.protocol.ProtocolMessageEncoder;
import com.yupi.yurpc.protocol.ProtocolMessageTypeEnum;
import com.yupi.yurpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

public class TcpServerHandler implements Handler<NetSocket> {
    @Override
    public void handle(NetSocket netSocket) {
        // 这里在new TcpBufferHandlerWrapper对象的时候
        // ()中使用lambda表达式快速构建了一个Handler<Buffer>的实现类，内部重写了handle方法
        // 这里内部实现类也就是后面的bufferHandler
        TcpBufferHandlerWrapper bufferHandler = new TcpBufferHandlerWrapper(buffer -> {
            // 接受请求并解码
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();

            // 处理请求(反射拿到对象然后调用目标方法 + 封装结果)
            RpcResponse rpcResponse = new RpcResponse();
            try {
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(result.getClass());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            //准备发送响应，编码
            //必须手动指明消息类型
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> response = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(response);
                netSocket.write(encode);
            } catch (IOException e) {
                throw new RuntimeException("协议消息编码错误");
            }
        });
        netSocket.handler(bufferHandler);
    }
}
