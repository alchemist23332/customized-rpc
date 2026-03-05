package com.yupi.yurpc.fault.retry;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.yupi.yurpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class ExponentialBackoffRetryStrategy implements RetryStrategy {

    private static final long INITIAL_INTERVAL_MILLIS = 1000L;

    private static final long MAX_INTERVAL_MILLIS = 30000L;

    private static final int MAX_ATTEMPTS = 5;

    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .withWaitStrategy(attempt -> {
                    long exp = Math.min(attempt.getAttemptNumber() - 1, 30);
                    long waitTime = INITIAL_INTERVAL_MILLIS * (1L << exp);
                    return Math.min(waitTime, MAX_INTERVAL_MILLIS);
                })
                .withStopStrategy(StopStrategies.stopAfterAttempt(MAX_ATTEMPTS))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数 {}", attempt.getAttemptNumber());
                    }
                })
                .build();
        return retryer.call(callable);
    }
}