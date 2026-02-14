package com.yupi.yurpc.serializer;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
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

    private static final Map<String, Class<?>> PRIMITIVE_NAME_MAP = new HashMap<>();

    static {
        PRIMITIVE_WRAPPER_MAP.put(boolean.class, Boolean.class);
        PRIMITIVE_WRAPPER_MAP.put(byte.class, Byte.class);
        PRIMITIVE_WRAPPER_MAP.put(char.class, Character.class);
        PRIMITIVE_WRAPPER_MAP.put(short.class, Short.class);
        PRIMITIVE_WRAPPER_MAP.put(int.class, Integer.class);
        PRIMITIVE_WRAPPER_MAP.put(long.class, Long.class);
        PRIMITIVE_WRAPPER_MAP.put(float.class, Float.class);
        PRIMITIVE_WRAPPER_MAP.put(double.class, Double.class);

        PRIMITIVE_NAME_MAP.put("boolean", boolean.class);
        PRIMITIVE_NAME_MAP.put("byte", byte.class);
        PRIMITIVE_NAME_MAP.put("char", char.class);
        PRIMITIVE_NAME_MAP.put("short", short.class);
        PRIMITIVE_NAME_MAP.put("int", int.class);
        PRIMITIVE_NAME_MAP.put("long", long.class);
        PRIMITIVE_NAME_MAP.put("float", float.class);
        PRIMITIVE_NAME_MAP.put("double", double.class);
        PRIMITIVE_NAME_MAP.put("void", void.class);
    }

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        return JSONUtil.toJsonStr(obj).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        String json = new String(bytes, StandardCharsets.UTF_8);
        if (RpcRequest.class.equals(clazz)) {
            return (T) deserializeRpcRequest(json);
        }
        if (RpcResponse.class.equals(clazz)) {
            return (T) deserializeRpcResponse(json);
        }
        return JSONUtil.toBean(json, clazz);
    }

    private RpcRequest deserializeRpcRequest(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName(jsonObject.getStr("serviceName"));
        rpcRequest.setMethodName(jsonObject.getStr("methodName"));

        Class<?>[] parameterTypes = deserializeParameterTypes(jsonObject.getJSONArray("parameterTypes"));
        rpcRequest.setParameterTypes(parameterTypes);

        Object[] args = deserializeArgs(jsonObject.getJSONArray("args"), parameterTypes);
        rpcRequest.setArgs(args);
        return rpcRequest;
    }

    private RpcResponse deserializeRpcResponse(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setMessage(jsonObject.getStr("message"));

        Class<?> dataType = deserializeClass(jsonObject.getStr("dataType"));
        rpcResponse.setDataType(dataType);

        Object data = jsonObject.get("data");
        if (data != null && dataType != null && !isAssignable(dataType, data.getClass())) {
            data = convertValue(data, dataType);
        }
        rpcResponse.setData(data);

        Object exceptionObject = jsonObject.get("exception");
        if (exceptionObject instanceof Exception) {
            rpcResponse.setException((Exception) exceptionObject);
        }
        return rpcResponse;
    }

    private Class<?>[] deserializeParameterTypes(JSONArray parameterTypeArray) {
        if (parameterTypeArray == null) {
            return new Class<?>[0];
        }
        Class<?>[] parameterTypes = new Class<?>[parameterTypeArray.size()];
        for (int i = 0; i < parameterTypeArray.size(); i++) {
            parameterTypes[i] = deserializeClass(parameterTypeArray.getStr(i));
        }
        return parameterTypes;
    }

    private Object[] deserializeArgs(JSONArray argsArray, Class<?>[] parameterTypes) {
        if (argsArray == null) {
            return new Object[0];
        }
        Object[] args = new Object[argsArray.size()];
        for (int i = 0; i < argsArray.size(); i++) {
            Object arg = argsArray.get(i);
            if (parameterTypes.length <= i || parameterTypes[i] == null || arg == null) {
                args[i] = arg;
                continue;
            }
            Class<?> parameterType = parameterTypes[i];
            if (isAssignable(parameterType, arg.getClass())) {
                args[i] = arg;
                continue;
            }
            args[i] = convertValue(arg, parameterType);
        }
        return args;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        Class<?> wrapperTargetType = wrapperType(targetType);
        if (value instanceof JSONObject || value instanceof JSONArray) {
            return JSONUtil.toBean(JSONUtil.toJsonStr(value), wrapperTargetType);
        }
        return Convert.convert(wrapperTargetType, value);
    }

    private Class<?> deserializeClass(String className) {
        if (className == null) {
            return null;
        }
        String normalizedClassName = normalizeClassName(className);
        Class<?> primitiveClass = PRIMITIVE_NAME_MAP.get(normalizedClassName);
        if (primitiveClass != null) {
            return primitiveClass;
        }
        try {
            return Class.forName(normalizedClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("反序列化类型失败: " + className, e);
        }
    }

    private String normalizeClassName(String className) {
        String normalized = className.trim();
        if (normalized.startsWith("class ")) {
            return normalized.substring("class ".length());
        }
        if (normalized.startsWith("interface ")) {
            return normalized.substring("interface ".length());
        }
        return normalized;
    }

    private boolean isAssignable(Class<?> targetType, Class<?> sourceType) {
        return wrapperType(targetType).isAssignableFrom(wrapperType(sourceType));
    }

    private Class<?> wrapperType(Class<?> type) {
        return type.isPrimitive() ? PRIMITIVE_WRAPPER_MAP.get(type) : type;
    }
}
