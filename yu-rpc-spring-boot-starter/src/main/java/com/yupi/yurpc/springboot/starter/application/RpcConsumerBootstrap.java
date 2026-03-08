package com.yupi.yurpc.springboot.starter.application;

import com.yupi.yurpc.proxy.ServiceProxyFactory;
import com.yupi.yurpc.springboot.starter.annoation.RpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

// 消费者启动类
public class RpcConsumerBootstrap implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // 获取类的每个字段,并检查
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            // 判断该字段是否被RpcReference注解
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                Class<?> interfaceClass = rpcReference.interfaceClass();
                if (interfaceClass == void.class) {
                    interfaceClass = field.getType();
                }
                // 设置访问权限
                field.setAccessible(true);
                // 创建代理对象
                Object proxy = ServiceProxyFactory.getProxy(interfaceClass);
                try {
                    // 注入代理对象
                    field.set(bean, proxy);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

}
