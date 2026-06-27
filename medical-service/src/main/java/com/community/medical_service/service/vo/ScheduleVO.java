package com.community.medical_service.vo;

import com.community.medical_service.entity.Schedule;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ScheduleVO extends Schedule {
    private String doctorName; // 医生姓名
    private String department; // 科室（如果需要）
}