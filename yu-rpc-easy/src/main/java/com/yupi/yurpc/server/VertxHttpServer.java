package com.yupi.yurpc.server;

import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer{
    /**
     * 启动服务器
     *
     * @param port 端口
     */
    @Override
    public void doStart(int port) {
        // 创建Vert.x实例, 用于创建HTTP服务器
        Vertx vertx = Vertx.vertx();
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();
        // 处理HTTP请求
        server.requestHandler(new HttpServerHandler());
        // 启动 HTTP 服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("HTTP server started on port " + port);
            } else {
                System.out.println("Failed to start HTTP server: " + result.cause());
            }
        });
    }
}
