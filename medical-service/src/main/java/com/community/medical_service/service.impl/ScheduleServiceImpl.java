// 文件位置：src/main/java/com/community/medical_service/service/impl/ScheduleServiceImpl.java
package com.community.medical_service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.medical_service.entity.Appointment;
import com.community.medical_service.entity.Schedule;
import com.community.medical_service.mapper.AppointmentMapper;
import com.community.medical_service.mapper.ScheduleMapper;
import com.community.medical_service.service.ScheduleService;
import com.community.medical_service.vo.ScheduleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public List<ScheduleVO> listWithDoctorName(QueryWrapper<Schedule> wrapper) {
        return scheduleMapper.listWithDoctorName(wrapper);
    }

    @Override
    public ScheduleVO getByIdWithDoctorName(Long id) {
        return scheduleMapper.getByIdWithDoctorName(id);
    }

    @Override
    public List<ScheduleVO> listByDoctorId(Long doctorId) {
        return scheduleMapper.listByDoctorId(doctorId);
    }

    @Override
    public List<ScheduleVO> listByDate(String workDate) {
        return scheduleMapper.listByDate(workDate);
    }

    @Override
    public boolean hasActiveAppointments(Long scheduleId) {
        Long count = appointmentMapper.selectCount(
                new QueryWrapper<Appointment>()
                        .eq("schedule_id", scheduleId)
                        .eq("status", 0)
        );
        return count > 0;
    }

    @Override
    public List<String> getAvailableDates() {
        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT work_date")
                .ge("work_date", LocalDate.now())
                .orderByAsc("work_date");

        List<Schedule> schedules = this.list(wrapper);
        return schedules.stream()
                .map(s -> s.getWorkDate().toString())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteExpiredSchedules() {
        // 获取所有过期排班
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("work_date", LocalDate.now());
        List<Schedule> expiredSchedules = this.list(queryWrapper);

        int deletedCount = 0;
        for (Schedule schedule : expiredSchedules) {
            // 检查是否有任何预约记录
            Long appointmentCount = appointmentMapper.selectCount(
                    new QueryWrapper<Appointment>()
                            .eq("schedule_id", schedule.getId())
            );

            // 只有没有任何预约记录的排班才能删除
            if (appointmentCount == 0) {
                boolean deleted = this.removeById(schedule.getId());
                if (deleted) {
                    deletedCount++;
                }
            }
        }

        return deletedCount;
    }

    @Override
    public int getExpiredCount() {
        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
        wrapper.lt("work_date", LocalDate.now());
        return Math.toIntExact(this.count(wrapper));
    }
}