package com.yupi.yurpc.fault.retry;

public interface RetryStrategyKeys {
    /**
     * 不重试
     */
    String NO = "no";
    /**
     * 固定间隔重试策略
     */
    String FIXED_INTERVAL = "fixedInterval";
}