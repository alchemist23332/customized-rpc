package com.yupi.yurpc.fault.tolerant;

import com.yupi.yurpc.model.RpcResponse;

import java.util.Map;

public interface TolerantStrategy {
    /**
     * 容错处理
     *
     * @param requestParams
     * @param e
     * @return
     */
    RpcResponse doTolerant(Map<String, Object> requestParams, Exception e);
}
