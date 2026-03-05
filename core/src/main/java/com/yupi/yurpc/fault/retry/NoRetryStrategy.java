package com.yupi.yurpc.fault.retry;

import com.yupi.yurpc.model.RpcResponse;

import java.util.concurrent.Callable;

public class NoRetryStrategy implements RetryStrategy{
    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
