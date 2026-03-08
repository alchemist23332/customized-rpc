package com.yupi.yurpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * 服务元注册信息
 */
public class ServiceMetaInfo {
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 服务版本
     */
    @Builder.Default
    private String serviceVersion = "1.0";
    /**
     * 服务地址
     */
    private String serviceHost;
    /**
     * 服务端口
     */
    private Integer servicePort;
    /**
     * 服务分组
     */
    @Builder.Default
    private String serviceGroup = "default";

    /**
     * 获取服务键
     * @return
     */
    public String getServiceKey() {
        return String.format("%s:%s", serviceName, serviceVersion);
    }

    /**
     * 获取服务节点键
     * @return
     */
    public String getServiceNodeKey() {
        return  String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort);
    }
    /**
     * 获取服务提供者地址
     * @return
     */
    public String getServiceAddress() {
        return String.format("%s:%s", serviceHost, servicePort);
    }
}
