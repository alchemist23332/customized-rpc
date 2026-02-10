package com.yupi.example.provider;

import com.yupi.example.common.model.User;
import com.yupi.example.common.service.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("远程成功获取User:" + user.getName());
        user.setName("白大帅");
        return user;
    }
}
