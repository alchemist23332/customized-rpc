package com.yupi.yurpc.springboot.starter.annoation;

import com.yupi.yurpc.springboot.starter.application.RpcConsumerBootstrap;
import com.yupi.yurpc.springboot.starter.application.RpcInitBootstrap;
import com.yupi.yurpc.springboot.starter.application.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author alchemist
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcConsumerBootstrap.class, RpcProviderBootstrap.class})
public @interface EnableRpc {
    /**
     * 是否需要启动服务器
     * @return
     */
    boolean needServer() default true;
}
