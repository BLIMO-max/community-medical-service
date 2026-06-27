// 文件位置：src/main/java/com/community/medical_service/entity/Appointment.java
package com.community.medical_service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long scheduleId;

    // 状态：0-待就诊，1-已完成，2-已取消，3-爽约，4-医生停诊
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}