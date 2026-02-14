package com.yupi.yurpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.yupi.yurpc.serializer.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpiLoader2 {
    private static Map<String, Map<String, Class<?>>> loadMap = new ConcurrentHashMap<>();
    private static Map<String, Object> instanceCache = new ConcurrentHashMap<>();
    private static final String[] SCAN_DIRS = new String[]{"META-INF/rpc/system/", "META-INF/rpc/custom/"};
    private static final Class<?>[] LOAD_CLASS_LIST = new Class[]{Serializer.class};
    public static void loadAll() throws IOException, ClassNotFoundException {
        for (var aClass : LOAD_CLASS_LIST) {
            load(aClass);
        }
    }
    public static <T> T getInstance(Class<T> tClass, String key) {
        String className = tClass.getName();
        Map<String, Class<?>> stringClassMap = loadMap.get(className);
        Class<?> targetClass = stringClassMap.get(key);
        String imlClass = targetClass.getName();
        try {
            instanceCache.put(imlClass, targetClass.newInstance());
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return (T) instanceCache.get(imlClass);
    }
    public static Map<String, Class<?>> load(Class<?> loadClass) throws IOException, ClassNotFoundException {
        // 扫描路径，用户自定义的 SPI 优先级高于系统 SPI
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        for (var dir : SCAN_DIRS) {
            List<URL> urls = ResourceUtil.getResources(dir + loadClass.getName());
            for (URL resource : urls) {
                try(InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("#") || line.isEmpty()) {
                            continue;
                        }
                        String[] strings = line.split("=");
                        if (strings.length >= 2) {
                            String key = strings[0].trim();
                            String className = strings[1].trim();
                            Class<?> aClass = Class.forName(className);
                            if (!keyClassMap.containsKey(key)) {
                                keyClassMap.put(key, aClass);
                            }
                        }
                    }
                }
            }
        }
        loadMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }
}
