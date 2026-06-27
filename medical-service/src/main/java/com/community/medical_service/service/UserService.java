package com.community.medical_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.medical_service.entity.User;

public interface UserService extends IService<User> {
    User login(String username, String password);
}