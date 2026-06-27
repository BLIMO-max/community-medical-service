package com.community.medical_service.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DbDebugMapper {

    @Select("SELECT 1")
    Integer ping();

    // 把 some_table 换成 hospital_db 里真实存在的表
    @Select("SELECT COUNT(*) FROM hospital_db.sys_user")
    Long countHospital();

    // 把 some_table 换成 sys 里真实存在的表；库名 sys 建议加反引号
    @Select("SELECT COUNT(*) FROM `sys`.sys_user")
    Long countSys();
}