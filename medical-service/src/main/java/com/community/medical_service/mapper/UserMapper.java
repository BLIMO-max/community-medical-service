package com.community.medical_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.medical_service.entity.User; // 引用你自己的User类
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}