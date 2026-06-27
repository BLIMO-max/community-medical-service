// 文件位置：src/main/java/com/community/medical_service/task/ScheduleTask.java
package com.community.medical_service.task;

import com.community.medical_service.service.AppointmentService;
import com.community.medical_service.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ScheduleTask {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 每天凌晨2点执行
     * 1. 将昨天的待就诊预约标记为爽约
     * 2. 清除已过期的排班
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredData() {
        // 1. 标记爽约
        String yesterday = LocalDate.now().minusDays(1).toString();
        int missedCount = appointmentService.batchMarkMissed(yesterday);
        System.out.println("[定时任务] 标记爽约记录：" + missedCount + " 条");

        // 2. 清除过期排班
        int deletedCount = scheduleService.deleteExpiredSchedules();
        System.out.println("[定时任务] 清除过期排班：" + deletedCount + " 条");
    }
}