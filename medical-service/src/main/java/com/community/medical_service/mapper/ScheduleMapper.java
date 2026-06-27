// 文件位置：src/main/java/com/community/medical_service/mapper/ScheduleMapper.java
package com.community.medical_service.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.medical_service.entity.Schedule;
import com.community.medical_service.vo.ScheduleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    /**
     * 带医生姓名的排班列表查询
     * 🔧 修复：移除固定的 WHERE u.role = 1，改为由调用方传入条件
     */
    @Select("SELECT s.*, u.real_name as doctor_name " +
            "FROM sys_schedule s " +
            "LEFT JOIN sys_user u ON s.doctor_id = u.id " +
            "${ew.customSqlSegment}")
    List<ScheduleVO> listWithDoctorName(@Param("ew") QueryWrapper<Schedule> wrapper);

    @Select("SELECT s.*, u.real_name as doctor_name " +
            "FROM sys_schedule s " +
            "LEFT JOIN sys_user u ON s.doctor_id = u.id " +
            "WHERE s.id = #{id}")
    ScheduleVO getByIdWithDoctorName(@Param("id") Long id);

    @Select("SELECT s.*, u.real_name as doctor_name " +
            "FROM sys_schedule s " +
            "LEFT JOIN sys_user u ON s.doctor_id = u.id " +
            "WHERE s.doctor_id = #{doctorId} " +
            "ORDER BY s.work_date DESC")
    List<ScheduleVO> listByDoctorId(@Param("doctorId") Long doctorId);

    @Select("SELECT s.*, u.real_name as doctor_name " +
            "FROM sys_schedule s " +
            "LEFT JOIN sys_user u ON s.doctor_id = u.id " +
            "WHERE s.work_date = #{workDate} " +
            "ORDER BY s.time_slot")
    List<ScheduleVO> listByDate(@Param("workDate") String workDate);

    /**
     * 释放号源（减少已预约人数）
     */
    @Update("UPDATE sys_schedule SET booked_count = booked_count - 1 " +
            "WHERE id = #{scheduleId} AND booked_count > 0")
    int decreaseBookedCount(@Param("scheduleId") Long scheduleId);

    /**
     * 重置号源（医生停诊时使用）
     */
    @Update("UPDATE sys_schedule SET booked_count = 0 WHERE id = #{scheduleId}")
    int resetBookedCount(@Param("scheduleId") Long scheduleId);
}