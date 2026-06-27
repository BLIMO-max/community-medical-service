// 文件位置：src/main/java/com/community/medical_service/controller/AppointmentController.java
package com.community.medical_service.controller;

import com.community.medical_service.common.Result;
import com.community.medical_service.entity.Appointment;
import com.community.medical_service.service.AppointmentService;
import com.community.medical_service.vo.AppointmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointment")
//@CrossOrigin
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // ==================== 原有接口 ====================

    @PostMapping("/book")
    public Result<?> book(@RequestParam Long userId, @RequestParam Long scheduleId) {
        try {
            boolean success = appointmentService.bookAppointment(userId, scheduleId);
            if (success) {
                return Result.success("预约成功！请按时就诊");
            } else {
                return Result.error("预约失败，请稍后重试");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public Result<List<AppointmentVO>> getUserAppointments(@PathVariable Long userId) {
        List<AppointmentVO> list = appointmentService.getUserAppointments(userId);
        return Result.success(list);
    }

    @GetMapping("/doctor/{doctorId}/patients")
    public Result<List<AppointmentVO>> getDoctorPatients(
            @PathVariable Long doctorId,
            @RequestParam(required = false) String date) {
        List<AppointmentVO> list = appointmentService.getDoctorPatients(doctorId, date);
        return Result.success(list);
    }

    @PutMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        boolean success = appointmentService.cancelAppointment(id);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    @PutMapping("/complete/{id}")
    public Result<?> complete(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment != null) {
            appointment.setStatus(1);
            appointmentService.updateById(appointment);
            return Result.success("已完成就诊");
        }
        return Result.error("预约不存在");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = appointmentService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败，记录不存在");
    }

    /**
     * 批量删除无效预约（已取消、爽约、停诊）
     * DELETE /appointment/invalid?userId=1
     */
    @DeleteMapping("/invalid")
    public Result<?> deleteInvalid(@RequestParam Long userId) {
        boolean success = appointmentService.lambdaUpdate()
                .eq(Appointment::getUserId, userId)
                .in(Appointment::getStatus, 2, 3, 4)
                .remove();
        return success ? Result.success("清除成功") : Result.error("清除失败");
    }

    @DeleteMapping("/cancelled")
    public Result<?> deleteCancelled(@RequestParam Long userId) {
        boolean success = appointmentService.lambdaUpdate()
                .eq(Appointment::getUserId, userId)
                .in(Appointment::getStatus, 2, 3, 4)
                .remove();
        return success ? Result.success("清除成功") : Result.error("清除失败");
    }

    // ==================== 新增接口 ====================

    /**
     * 标记为爽约（患者未按时就诊）
     * PUT /appointment/missed/{id}
     */
    @PutMapping("/missed/{id}")
    public Result<?> markAsMissed(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment != null && appointment.getStatus() == 0) {
            appointment.setStatus(3); // 3 = 爽约
            appointmentService.updateById(appointment);
            return Result.success("已标记为爽约");
        }
        return Result.error("操作失败");
    }

    /**
     * 医生停诊 - 取消某排班下的所有预约
     * PUT /appointment/doctor-cancel?scheduleId=1
     */
    @PutMapping("/doctor-cancel")
    public Result<?> doctorCancelSchedule(@RequestParam Long scheduleId) {
        try {
            int count = appointmentService.doctorCancelSchedule(scheduleId);
            return Result.success("已取消 " + count + " 个预约");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量标记爽约（定时任务调用）
     * PUT /appointment/batch-missed?date=2026-01-15
     */
    @PutMapping("/batch-missed")
    public Result<?> batchMarkMissed(@RequestParam String date) {
        int count = appointmentService.batchMarkMissed(date);
        return Result.success("已标记 " + count + " 个爽约记录");
    }

    /**
     * 获取患者爽约次数
     * GET /appointment/missed-count/{userId}
     */
    @GetMapping("/missed-count/{userId}")
    public Result<Long> getMissedCount(@PathVariable Long userId) {
        long count = appointmentService.lambdaQuery()
                .eq(Appointment::getUserId, userId)
                .eq(Appointment::getStatus, 3)
                .count();
        return Result.success(count);
    }
}