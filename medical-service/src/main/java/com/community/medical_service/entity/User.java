// 文件位置：src/main/java/com/community/medical_service/entity/User.java
package com.community.medical_service.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;

    @TableField("real_name")
    private String realName;

    private Integer role;
    private String phone;
    private String gender;
    private Integer age;
    private String bloodType;
    private Integer status;

    /**
     * 创建时间 - 插入时由数据库自动生成，不手动设置
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    /**
     * 更新时间 - 插入和更新时由数据库自动生成
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}