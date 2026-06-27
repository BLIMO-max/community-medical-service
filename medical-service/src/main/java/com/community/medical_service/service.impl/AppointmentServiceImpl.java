// 文件位置：src/main/java/com/community/medical_service/service/impl/AppointmentServiceImpl.java
package com.community.medical_service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.medical_service.entity.Appointment;
import com.community.medical_service.entity.Schedule;
import com.community.medical_service.mapper.AppointmentMapper;
import com.community.medical_service.mapper.ScheduleMapper;
import com.community.medical_service.service.AppointmentService;
import com.community.medical_service.vo.AppointmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    /**
     * 预约挂号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bookAppointment(Long userId, Long scheduleId) {
        // 1. 检查是否重复预约（同一个排班，同一个人只能约一次）
        QueryWrapper<Appointment> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("schedule_id", scheduleId);
        wrapper.eq("status", 0); // 只查待就诊的
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("您已预约过该排班，请勿重复操作");
        }

        // 2. 检查排班是否存在且日期是否有效
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }
        if (schedule.getWorkDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("该排班日期已过，无法预约");
        }
        if (schedule.getBookedCount() >= schedule.getMaxCount()) {
            throw new RuntimeException("号源已满，无法预约");
        }

        // 3. 扣减号源（使用乐观锁，防止超卖）
        int rows = appointmentMapper.increaseBookedCount(scheduleId);
        if (rows == 0) {
            throw new RuntimeException("手慢了，号源已抢光！");
        }

        // 4. 生成预约记录
        Appointment appointment = new Appointment();
        appointment.setUserId(userId);
        appointment.setScheduleId(scheduleId);
        appointment.setStatus(0); // 0 = 待就诊
        appointment.setCreateTime(LocalDateTime.now());

        return this.save(appointment);
    }

    /**
     * 获取用户的预约列表（带医生信息）
     */
    @Override
    public List<AppointmentVO> getUserAppointments(Long userId) {
        return appointmentMapper.getUserAppointments(userId);
    }

    /**
     * 获取医生的预约患者列表
     */
    @Override
    public List<AppointmentVO> getDoctorPatients(Long doctorId, String date) {
        return appointmentMapper.getDoctorPatients(doctorId, date);
    }

    /**
     * 取消预约（患者主动取消）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelAppointment(Long id) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new RuntimeException("预约记录不存在");
        }
        if (appointment.getStatus() != 0) {
            throw new RuntimeException("当前状态无法取消");
        }

        // 检查是否已过就诊日期
        Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule != null && schedule.getWorkDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("就诊日期已过，无法取消");
        }

        // 更新状态为已取消
        appointment.setStatus(2); // 2 = 已取消
        boolean success = this.updateById(appointment);

        // 释放号源
        if (success) {
            scheduleMapper.decreaseBookedCount(appointment.getScheduleId());
        }

        return success;
    }

    /**
     * 医生停诊 - 取消某排班下的所有待就诊预约
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doctorCancelSchedule(Long scheduleId) {
        // 检查排班是否存在
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }

        // 将所有待就诊的预约改为医生停诊状态
        int count = appointmentMapper.updateStatusByScheduleId(scheduleId, 0, 4);

        // 释放所有号源
        if (count > 0) {
            scheduleMapper.resetBookedCount(scheduleId);
        }

        return count;
    }

    /**
     * 批量标记爽约（将过期的待就诊预约标记为爽约）
     */
    @Override
    public int batchMarkMissed(String date) {
        return appointmentMapper.batchMarkMissed(date);
    }

    /**
     * 完成就诊
     */
    public boolean completeAppointment(Long id) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new RuntimeException("预约记录不存在");
        }
        if (appointment.getStatus() != 0) {
            throw new RuntimeException("当前状态无法完成");
        }

        appointment.setStatus(1); // 1 = 已完成
        return this.updateById(appointment);
    }

    /**
     * 标记为爽约（患者未按时就诊）
     */
    public boolean markAsMissed(Long id) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new RuntimeException("预约记录不存在");
        }
        if (appointment.getStatus() != 0) {
            throw new RuntimeException("当前状态无法标记");
        }

        appointment.setStatus(3); // 3 = 爽约
        boolean success = this.updateById(appointment);

        // 释放号源
        if (success) {
            scheduleMapper.decreaseBookedCount(appointment.getScheduleId());
        }

        return success;
    }

    /**
     * 物理删除预约记录
     */
    public boolean deleteAppointment(Long id) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            return false;
        }

        // 如果是待就诊状态，需要释放号源
        if (appointment.getStatus() == 0) {
            scheduleMapper.decreaseBookedCount(appointment.getScheduleId());
        }

        return this.removeById(id);
    }

    /**
     * 批量删除无效预约（已取消、爽约、停诊）
     */
    public int deleteInvalidAppointments(Long userId) {
        return appointmentMapper.deleteInvalidByUserId(userId);
    }
}