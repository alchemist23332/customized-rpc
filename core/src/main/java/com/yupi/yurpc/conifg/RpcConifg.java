package com.yupi.yurpc.conifg;

import lombok.Data;

@Data
public class RpcConifg {
    /**
     * 协议名称
     */
    private String name = "yu-rpc";
    /**
     * 服务端口
     */
    private int port = 8080;
    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";
    /**
     * 版本号
     */
    private String version = "1.0";

}
