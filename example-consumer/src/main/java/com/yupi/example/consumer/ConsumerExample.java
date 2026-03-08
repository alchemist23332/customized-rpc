package com.yupi.example.consumer;

import com.yupi.example.common.model.User;
import com.yupi.example.common.service.UserService;
import com.yupi.yurpc.bootstrap.ConsumerBootstrap;
import com.yupi.yurpc.conifg.RpcConifg;
import com.yupi.yurpc.constant.RpcConstant;
import com.yupi.yurpc.proxy.ServiceProxyFactory;
import com.yupi.yurpc.spi.SpiLoader;
import com.yupi.yurpc.utils.ConfigUtils;

public class ConsumerExample {
    public static void main(String[] args) {
        // Rpc框架初始化
        ConsumerBootstrap.init();

        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        // 第一次调用
        User newUser = userService.getUser(new User("黑大帅"));
        System.out.println(newUser);
//        // 第二次调用
//        newUser = userService.getUser(new User("黑大帅"));
//        System.out.println(newUser);
//        // 第三次调用
//        newUser = userService.getUser(new User("黑大帅"));
//        System.out.println(newUser);
    }
}
