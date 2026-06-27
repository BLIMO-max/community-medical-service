package com.community.medical_service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("sys_schedule")
public class Schedule {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long doctorId; // 关联医生ID

    // 格式化日期，确保前端传 "2023-10-01" 能自动转成 Java 对象
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    private Integer timeSlot; // 1:上午 2:下午

    private Integer maxCount; // 最大号源 (例如 30)

    private Integer bookedCount; // 已经预约的人数
}