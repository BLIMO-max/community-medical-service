// 文件位置：src/main/java/com/community/medical_service/entity/Medicine.java
package com.community.medical_service.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_medicine")
public class Medicine {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private String name;
    private String specification;
    private String unit;
    private Double price;
    private Integer stock;
    private Integer minStock;
    private String manufacturer;
    private String remarks;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}