// 文件位置：src/main/java/com/community/medical_service/controller/ScheduleController.java
package com.community.medical_service.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.medical_service.common.Result;
import com.community.medical_service.entity.Schedule;
import com.community.medical_service.service.ScheduleService;
import com.community.medical_service.vo.ScheduleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedule")
//@CrossOrigin
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 添加排班接口 (管理员用)
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Schedule schedule) {
        if (schedule.getDoctorId() == null || schedule.getWorkDate() == null) {
            return Result.error("医生和日期不能为空");
        }

        // 检查是否重复
        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
        wrapper.eq("doctor_id", schedule.getDoctorId());
        wrapper.eq("work_date", schedule.getWorkDate());
        wrapper.eq("time_slot", schedule.getTimeSlot());

        if (scheduleService.count(wrapper) > 0) {
            return Result.error("该医生此时段已有排班，请勿重复添加");
        }

        schedule.setBookedCount(0);
        scheduleService.save(schedule);
        return Result.success("添加成功");
    }

    /**
     * 查询排班列表 (患者用) - 只返回今天及以后的排班
     * 🔧 修复：手动添加 u.role = 1 条件
     */
    @GetMapping("/list")
    public Result<List<ScheduleVO>> list(@RequestParam(required = false) Long doctorId) {
        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();

        // 🔧 只查询医生角色的排班
        wrapper.eq("u.role", 1);

        if (doctorId != null) {
            wrapper.eq("doctor_id", doctorId);
        }

        // 只查询今天及以后的排班
        wrapper.ge("work_date", LocalDate.now());
        wrapper.orderByAsc("work_date", "time_slot");

        List<ScheduleVO> list = scheduleService.listWithDoctorName(wrapper);
        return Result.success(list);
    }

    /**
     * 查询所有排班（管理员用）
     * includeExpired: true=包含过期, false=只显示未过期
     * 🔧 修复：手动添加 u.role = 1 条件
     */
    @GetMapping("/all")
    public Result<List<ScheduleVO>> listAll(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false, defaultValue = "true") Boolean includeExpired) {
        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();

        // 🔧 只查询医生角色的排班
        wrapper.eq("u.role", 1);

        if (doctorId != null) {
            wrapper.eq("doctor_id", doctorId);
        }

        // 如果不包含过期，只查询今天及以后
        if (!includeExpired) {
            wrapper.ge("work_date", LocalDate.now());
        }

        wrapper.orderByAsc("work_date", "time_slot");
        List<ScheduleVO> list = scheduleService.listWithDoctorName(wrapper);
        return Result.success(list);
    }

    /**
     * 查询单个排班详情
     */
    @GetMapping("/{id}")
    public Result<ScheduleVO> getById(@PathVariable Long id) {
        ScheduleVO schedule = scheduleService.getByIdWithDoctorName(id);
        if (schedule != null) {
            return Result.success(schedule);
        }
        return Result.error("排班不存在");
    }

    /**
     * 查询某医生的排班（只返回未来的）
     */
    @GetMapping("/doctor/{doctorId}")
    public Result<List<ScheduleVO>> listByDoctorId(@PathVariable Long doctorId) {
        List<ScheduleVO> list = scheduleService.listByDoctorId(doctorId);
        // 过滤掉过期的
        list = list.stream()
                .filter(s -> !s.getWorkDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 查询某日期的排班
     */
    @GetMapping("/date/{workDate}")
    public Result<List<ScheduleVO>> listByDate(@PathVariable String workDate) {
        List<ScheduleVO> list = scheduleService.listByDate(workDate);
        return Result.success(list);
    }

    /**
     * 删除排班 (管理员用)
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean hasActiveAppointments = scheduleService.hasActiveAppointments(id);
        if (hasActiveAppointments) {
            return Result.error("该排班有待就诊的预约，无法删除");
        }

        boolean success = scheduleService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 更新排班信息
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Schedule schedule) {
        Schedule existing = scheduleService.getById(schedule.getId());
        if (existing != null && existing.getWorkDate().isBefore(LocalDate.now())) {
            return Result.error("过期的排班无法修改");
        }

        boolean success = scheduleService.updateById(schedule);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 获取可预约的日期列表
     */
    @GetMapping("/available-dates")
    public Result<List<String>> getAvailableDates() {
        List<String> dates = scheduleService.getAvailableDates();
        return Result.success(dates);
    }

    /**
     * 清除过期的排班（只删除没有预约记录的）
     */
    @DeleteMapping("/expired")
    public Result<String> deleteExpiredSchedules() {
        int count = scheduleService.deleteExpiredSchedules();
        return Result.success("已清除 " + count + " 条过期排班");
    }

    /**
     * 获取过期排班数量
     */
    @GetMapping("/expired/count")
    public Result<Integer> getExpiredCount() {
        int count = scheduleService.getExpiredCount();
        return Result.success(count);
    }
}