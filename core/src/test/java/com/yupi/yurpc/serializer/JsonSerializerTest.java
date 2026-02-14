package com.yupi.yurpc.serializer;

import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.RpcResponse;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class JsonSerializerTest {

    private final JsonSerializer jsonSerializer = new JsonSerializer();

    @Test
    public void testSerializeAndDeserializeRpcRequest() throws IOException {
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName("userService")
                .methodName("save")
                .parameterTypes(new Class<?>[]{Integer.class, User.class})
                .args(new Object[]{1, new User("tom", 18)})
                .build();

        byte[] bytes = jsonSerializer.serialize(rpcRequest);
        RpcRequest deserialized = jsonSerializer.deserialize(bytes, RpcRequest.class);

        Assert.assertEquals("userService", deserialized.getServiceName());
        Assert.assertEquals("save", deserialized.getMethodName());
        Assert.assertEquals(Integer.class, deserialized.getArgs()[0].getClass());
        Assert.assertTrue(deserialized.getArgs()[1] instanceof User);
        User user = (User) deserialized.getArgs()[1];
        Assert.assertEquals("tom", user.getName());
    }

    @Test
    public void testSerializeAndDeserializeRpcResponse() throws IOException {
        RpcResponse rpcResponse = RpcResponse.builder()
                .data(new User("jerry", 20))
                .dataType(User.class)
                .message("ok")
                .build();

        byte[] bytes = jsonSerializer.serialize(rpcResponse);
        RpcResponse deserialized = jsonSerializer.deserialize(bytes, RpcResponse.class);

        Assert.assertTrue(deserialized.getData() instanceof User);
        User data = (User) deserialized.getData();
        Assert.assertEquals("jerry", data.getName());
    }

    public static class User {
        private String name;
        private Integer age;

        public User() {
        }

        public User(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }
    }
}
