// 文件位置：src/main/java/com/community/medical_service/service/AppointmentService.java
package com.community.medical_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.medical_service.entity.Appointment;
import com.community.medical_service.vo.AppointmentVO;

import java.util.List;

public interface AppointmentService extends IService<Appointment> {
    boolean bookAppointment(Long userId, Long scheduleId);
    List<AppointmentVO> getUserAppointments(Long userId);
    List<AppointmentVO> getDoctorPatients(Long doctorId, String date);
    boolean cancelAppointment(Long id);
    int doctorCancelSchedule(Long scheduleId);
    int batchMarkMissed(String date);
}