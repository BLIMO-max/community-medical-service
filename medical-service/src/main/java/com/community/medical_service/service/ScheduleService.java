// 文件位置：src/main/java/com/community/medical_service/service/ScheduleService.java
package com.community.medical_service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.medical_service.entity.Schedule;
import com.community.medical_service.vo.ScheduleVO;

import java.util.List;

public interface ScheduleService extends IService<Schedule> {

    List<ScheduleVO> listWithDoctorName(QueryWrapper<Schedule> wrapper);

    ScheduleVO getByIdWithDoctorName(Long id);

    List<ScheduleVO> listByDoctorId(Long doctorId);

    List<ScheduleVO> listByDate(String workDate);

    boolean hasActiveAppointments(Long scheduleId);

    List<String> getAvailableDates();

    int deleteExpiredSchedules();

    int getExpiredCount();
}