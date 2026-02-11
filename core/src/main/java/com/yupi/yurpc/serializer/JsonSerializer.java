package com.yupi.yurpc.serializer;

import cn.hutool.json.JSONUtil;
import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.RpcResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON 序列化器
 */
public class JsonSerializer implements Serializer {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = new HashMap<>();

    static {
        PRIMITIVE_WRAPPER_MAP.put(boolean.class, Boolean.class);
        PRIMITIVE_WRAPPER_MAP.put(byte.class, Byte.class);
        PRIMITIVE_WRAPPER_MAP.put(char.class, Character.class);
        PRIMITIVE_WRAPPER_MAP.put(short.class, Short.class);
        PRIMITIVE_WRAPPER_MAP.put(int.class, Integer.class);
        PRIMITIVE_WRAPPER_MAP.put(long.class, Long.class);
        PRIMITIVE_WRAPPER_MAP.put(float.class, Float.class);
        PRIMITIVE_WRAPPER_MAP.put(double.class, Double.class);
    }

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        return JSONUtil.toJsonStr(obj).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        T obj = JSONUtil.toBean(new String(bytes, StandardCharsets.UTF_8), clazz);
        if (obj instanceof RpcRequest) {
            handleRpcRequest((RpcRequest) obj);
        }
        if (obj instanceof RpcResponse) {
            handleRpcResponse((RpcResponse) obj);
        }
        return obj;
    }

    /**
     * 由于 JSON 反序列化后对象类型会丢失，需要根据请求中的参数类型信息重新转换
     */
    private void handleRpcRequest(RpcRequest rpcRequest) {
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getArgs();
        if (parameterTypes == null || args == null) {
            return;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            Object arg = args[i];
            if (arg == null || isAssignable(parameterType, arg.getClass())) {
                continue;
            }
            args[i] = JSONUtil.toBean(JSONUtil.toJsonStr(arg), wrapperType(parameterType));
        }
    }

    /**
     * 根据 dataType 还原响应中的 data 字段类型
     */
    private void handleRpcResponse(RpcResponse rpcResponse) {
        Class<?> dataType = rpcResponse.getDataType();
        Object data = rpcResponse.getData();
        if (data == null || dataType == null || isAssignable(dataType, data.getClass())) {
            return;
        }
        rpcResponse.setData(JSONUtil.toBean(JSONUtil.toJsonStr(data), wrapperType(dataType)));
    }

    private boolean isAssignable(Class<?> targetType, Class<?> sourceType) {
        return wrapperType(targetType).isAssignableFrom(wrapperType(sourceType));
    }

    private Class<?> wrapperType(Class<?> type) {
        return type.isPrimitive() ? PRIMITIVE_WRAPPER_MAP.get(type) : type;
    }
}
