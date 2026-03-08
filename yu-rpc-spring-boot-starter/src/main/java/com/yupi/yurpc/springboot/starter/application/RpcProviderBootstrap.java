package com.yupi.yurpc.springboot.starter.application;

import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.conifg.RegistryConfig;
import com.yupi.yurpc.conifg.RpcConifg;
import com.yupi.yurpc.model.ServiceMetaInfo;
import com.yupi.yurpc.registry.LocalRegistry;
import com.yupi.yurpc.registry.Registry;
import com.yupi.yurpc.registry.RegistryFactory;
import com.yupi.yurpc.springboot.starter.annoation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

// 服务提供者启动类
public class RpcProviderBootstrap implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> implClass = bean.getClass();
        // 判断类上是否有RpcService注解
        RpcService rpcService = implClass.getAnnotation(RpcService.class);
        if (rpcService != null) {
            // 获取接口类
            Class<?> interfaceClass = rpcService.interfaceClass();
            if (interfaceClass == void.class) {
                // 若用户未写明接口类，自动获取该Bean类实现的第一个接口，并将其作为要注册的服务接口
                interfaceClass = implClass.getInterfaces()[0];
            }
            String serviceName = interfaceClass.getName();
            String serviceVersion = rpcService.serviceVersion();
            final RpcConifg rpcConfig = RpcApplication.getRpcConfig();
            // 1.本地注册
            LocalRegistry.register(serviceName, implClass);
            // 2.配置服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            ServiceMetaInfo myService = ServiceMetaInfo.builder()
                    .serviceName(serviceName)
                    .serviceHost(rpcConfig.getServerHost())
                    .servicePort(rpcConfig.getServerPort())
                    .serviceVersion("1.0")
                    .serviceGroup("default")
                    .build();
            try {
                Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
                registry.register(myService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }


}
