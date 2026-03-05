package com.yupi.yurpc.fault.tolerant;

import com.yupi.yurpc.model.RpcResponse;

import java.util.Map;

/**
 * 快速失败容错策略
 */
public class FailFastTolerantStrategy implements TolerantStrategy{

    @Override
    public RpcResponse doTolerant(Map<String, Object> requestParams, Exception e) {
        throw new RuntimeException("服务调用出错", e);
    }
}
