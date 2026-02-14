package com.yupi.yurpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.yupi.yurpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpiLoader {
    /**
     * 存储已加载的类：接口名 =>（key => 实现类）
     */
    private static Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();
    
    /**
     * 对象实例缓存（避免重复 new），类路径 => 对象实例，单例模式
     */
    private static Map<String, Object> instanceCache = new ConcurrentHashMap<>();
    
    /**
     * 系统 SPI 目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";
    
    /**
     * 用户自定义 SPI 目录
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";
    
    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_CUSTOM_SPI_DIR, RPC_SYSTEM_SPI_DIR};
    
    /**
     * 动态加载的类列表
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(
        Serializer.class
    );
    
    /**
     * 加载所有类型
     */
    public static void loadAll() {
        log.info("加载所有 SPI");
        for (Class<?> aClass : LOAD_CLASS_LIST) {
            load(aClass);
        }
    }
    
    /**
     * 获取某个接口的实例
     */
    public static <T> T getInstance(Class<T> tClass, String key) {
        String tClassName = tClass.getName();
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);
        
        if (keyClassMap == null) {
            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型", tClassName));
        }
        
        if (!keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoader 的 %s 不存在 key=%s 的实现", tClassName, key));
        }
        
        // 获取到要加载的实现类型
        Class<?> implClass = keyClassMap.get(key);
        
        // 从实例缓存中加载指定类型的实例
        String implClassName = implClass.getName();
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                String errorMsg = String.format("%s 类实例化失败", implClassName);
                throw new RuntimeException(errorMsg, e);
            }
        }
        
        return (T) instanceCache.get(implClassName);
    }
    
    /**
     * 加载某个类型
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("加载类型为 {} 的 SPI", loadClass.getName());
        
        // 扫描路径，用户自定义的 SPI 优先级高于系统 SPI
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        
        for (String scanDir : SCAN_DIRS) {
            try {
                List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
                
                // 读取每个资源文件
                for (URL resource : resources) {
                    try (InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                         BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                        
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            // 跳过空行和注释行
                            line = line.trim();
                            if (line.isEmpty() || line.startsWith("#")) {
                                continue;
                            }
                            
                            String[] strArray = line.split("=");
                            if (strArray.length >= 2) {
                                String key = strArray[0].trim();
                                String className = strArray[1].trim();
                                
                                try {
                                    Class<?> clazz = Class.forName(className);
                                    // 自定义SPI优先，不覆盖已存在的key
                                    if (!keyClassMap.containsKey(key)) {
                                        keyClassMap.put(key, clazz);
                                    }
                                } catch (ClassNotFoundException e) {
                                    log.warn("SPI实现类 {} 未找到", className, e);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error("加载SPI资源失败: {}", scanDir, e);
            }
        }
        
        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }
    
    /**
     * 清空缓存（主要用于测试）
     */
    public static void clear() {
        loaderMap.clear();
        instanceCache.clear();
    }
    
    /**
     * 获取已加载的所有SPI实现
     */
    public static Map<String, Map<String, Class<?>>> getAllSpi() {
        return Collections.unmodifiableMap(loaderMap);
    }
}
