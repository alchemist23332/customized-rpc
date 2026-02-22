package com.yupi.yurpc.conifg;

import com.yupi.yurpc.loadbalancer.LoadBalancerKeys;
import com.yupi.yurpc.serializer.SerializerKeys;
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
    private int serverPort = 8080;
    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";
    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 是否开启mock
     */
    private boolean mock = false;

    /**
     * 序列化方式
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
    /**
     * 负载均衡配置
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

}
