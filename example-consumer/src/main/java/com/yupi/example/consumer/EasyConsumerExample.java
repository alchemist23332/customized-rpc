package com.yupi.example.consumer;

import com.yupi.example.common.model.User;
import com.yupi.example.common.service.UserService;
import com.yupi.yurpc.proxy.ServiceProxyFactory;

public class EasyConsumerExample {
    public static void main(String[] args) {
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = userService.getUser(new User("黑大帅"));
        if (user != null) {
            System.out.println("获取处理后的User:" + user.getName());
        } else {
            System.out.println("获取处理后的User为空");
        }
    }
}
