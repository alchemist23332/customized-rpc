package com.yupi.yurpc.springboot.starter.application;

import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.conifg.RpcConifg;
import com.yupi.yurpc.server.tcp.VertxTcpServer;
import com.yupi.yurpc.springboot.starter.annoation.EnableRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

// 框架启动类
@Slf4j
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取EnableRpc注解的属性
        boolean needServer = (boolean) importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName()).get("needServer");
        RpcApplication.init();
        final RpcConifg rpcConfig = RpcApplication.getRpcConfig();
        if (needServer) {
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            vertxTcpServer.doStart(rpcConfig.getServerPort());
            log.info("Rpc服务器已启动");
        } else {
            log.info("Rpc服务器未启用");
        }
    }
}
