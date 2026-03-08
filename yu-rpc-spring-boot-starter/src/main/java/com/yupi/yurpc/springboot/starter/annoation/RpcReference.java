package com.yupi.yurpc.springboot.starter.annoation;

import com.yupi.yurpc.constant.RpcConstant;
import com.yupi.yurpc.fault.retry.RetryStrategyKeys;
import com.yupi.yurpc.fault.tolerant.TolerantStrategyKeys;
import com.yupi.yurpc.loadbalancer.LoadBalancerKeys;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author alchemist
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {
    /**
     * 接口类
     * @return
     */
    Class<?> interfaceClass() default void.class;
    /**
     * 服务版本
     * @return
     */
    String ServiceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;
    /**
     * 负载均衡器
     * @return
     */
    String loadBalancer() default LoadBalancerKeys.ROUND_ROBIN;
    /**
     * 重试策略
     */
    String retryStrategy() default RetryStrategyKeys.NO;
    /**
     * 容错策略
     */
    String tolerantStrategy() default TolerantStrategyKeys.FAIL_FAST;
    /**
     * 模拟调用
     */
    boolean mock() default false;

}
