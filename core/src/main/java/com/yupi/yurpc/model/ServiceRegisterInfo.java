package com.yupi.yurpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRegisterInfo<T> {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务对应实现类
     * extends T 防止传入非对应的实现类,直接报错
     */
    private Class<? extends T> implClass;
}
