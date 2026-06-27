// 文件位置：src/main/java/com/community/medical_service/mapper/AppointmentMapper.java
package com.community.medical_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.medical_service.entity.Appointment;
import com.community.medical_service.vo.AppointmentVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    @Update("UPDATE sys_schedule SET booked_count = booked_count + 1 " +
            "WHERE id = #{scheduleId} AND booked_count < max_count")
    int increaseBookedCount(@Param("scheduleId") Long scheduleId);

    @Select("SELECT a.id, a.user_id, a.schedule_id, a.status, a.create_time, " +
            "u.id as patient_id, u.real_name as patient_name, u.gender as patient_gender, " +
            "u.age as patient_age, u.phone as patient_phone, " +
            "s.work_date, s.time_slot, " +
            "d.real_name as doctor_name, d.id as doctor_id " +
            "FROM sys_appointment a " +
            "JOIN sys_user u ON a.user_id = u.id " +
            "JOIN sys_schedule s ON a.schedule_id = s.id " +
            "JOIN sys_user d ON s.doctor_id = d.id " +
            "WHERE a.user_id = #{userId} " +
            "ORDER BY s.work_date DESC, s.time_slot")
    List<AppointmentVO> getUserAppointments(@Param("userId") Long userId);

    @Select("SELECT a.id, a.user_id, a.schedule_id, a.status, a.create_time, " +
            "u.id as patient_id, u.real_name as patient_name, u.gender as patient_gender, " +
            "u.age as patient_age, u.phone as patient_phone, " +
            "s.work_date, s.time_slot, " +
            "d.real_name as doctor_name " +
            "FROM sys_appointment a " +
            "JOIN sys_user u ON a.user_id = u.id " +
            "JOIN sys_schedule s ON a.schedule_id = s.id " +
            "JOIN sys_user d ON s.doctor_id = d.id " +
            "WHERE s.doctor_id = #{doctorId} " +
            "AND a.status = 0 " +
            "AND (#{date} IS NULL OR s.work_date = #{date}) " +
            "ORDER BY s.work_date, s.time_slot, a.create_time")
    List<AppointmentVO> getDoctorPatients(@Param("doctorId") Long doctorId, @Param("date") String date);

    /**
     * 批量更新预约状态
     */
    @Update("UPDATE sys_appointment SET status = #{newStatus} " +
            "WHERE schedule_id = #{scheduleId} AND status = #{oldStatus}")
    int updateStatusByScheduleId(@Param("scheduleId") Long scheduleId,
                                 @Param("oldStatus") Integer oldStatus,
                                 @Param("newStatus") Integer newStatus);

    /**
     * 批量标记爽约（将过期未就诊的预约标记为爽约）
     */
    @Update("UPDATE sys_appointment a " +
            "JOIN sys_schedule s ON a.schedule_id = s.id " +
            "SET a.status = 3 " +
            "WHERE a.status = 0 AND s.work_date < #{date}")
    int batchMarkMissed(@Param("date") String date);

    /**
     * 批量删除用户的无效预约（已取消、爽约、停诊）
     */
    @Delete("DELETE FROM sys_appointment " +
            "WHERE user_id = #{userId} AND status IN (2, 3, 4)")
    int deleteInvalidByUserId(@Param("userId") Long userId);
}