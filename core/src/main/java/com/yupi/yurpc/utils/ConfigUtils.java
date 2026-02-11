package com.yupi.yurpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

public class ConfigUtils {
    /**
     * 加载配置对象
     *
     * @param tClass
     * @param prefix
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }
    /**
     * 加载配置对象，支持区分环境
     *
     * @param tClass
     * @param prefix
     * @param environment
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        StringBuilder path = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            path.append("-").append(environment);
        }
        path.append(".properties");
        // 加载配置文件（传入path路径）
        Props props = new Props(path.toString());
        // 将配置文件转换成对象
        return props.toBean(tClass, prefix);

    }
}
