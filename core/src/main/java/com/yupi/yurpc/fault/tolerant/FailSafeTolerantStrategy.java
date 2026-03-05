package com.yupi.yurpc.fault.tolerant;

import com.yupi.yurpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 静默处理策略
 */
@Slf4j
public class FailSafeTolerantStrategy implements TolerantStrategy{

    @Override
    public RpcResponse doTolerant(Map<String, Object> requestParams, Exception e) {
        log.info("服务调用出错，采用静默处理策略");
        return new RpcResponse();
    }
}
