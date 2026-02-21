package com.yupi.yurpc.server.tcp;

import com.yupi.yurpc.server.HttpServer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;

public class VertxTcpServer implements HttpServer {
    /**
     * 启动服务器
     *
     * @param port 端口
     */
    @Override
    public void doStart(int port) {
        Vertx vertx = Vertx.vertx();
        // 创建NetServer(Tcp服务器)实例
        NetServer server = vertx.createNetServer();
        // 处理Tcp请求
        server.connectHandler(new TcpServerHandler());
        // 启动Tcp服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Tcp server started on port " + port);
            } else {
                System.out.println("Failed to start Tcp server: " + result.cause());
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(8080);
    }
}
