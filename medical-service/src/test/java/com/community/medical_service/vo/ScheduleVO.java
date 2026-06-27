// 新建文件：com.community.medical_service.vo.ScheduleVO.java
package com.community.medical_service.vo;

import com.community.medical_service.entity.Schedule;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ScheduleVO 继承自 Schedule，添加了医生姓名等额外字段
 * VO (View Object) 用于返回给前端的数据，比实体类包含更多信息
 */
@EqualsAndHashCode(callSuper = true) // 调用父类的 equals 和 hashCode
@Data
public class ScheduleVO extends Schedule {

    // 医生姓名（从 sys_user 表关联查询）
    private String doctorName;

    // 医生职称（如果需要）
    private String doctorTitle;

    // 科室名称（如果需要）
    private String department;

    // 可以在这里添加其他需要返回给前端的字段
}