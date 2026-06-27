// 文件位置：src/main/java/com/community/medical_service/task/AppointmentTask.java
package com.community.medical_service.task;

import com.community.medical_service.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AppointmentTask {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 每天凌晨1点执行，将昨天的待就诊预约标记为爽约
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void markMissedAppointments() {
        String yesterday = LocalDate.now().minusDays(1).toString();
        int count = appointmentService.batchMarkMissed(yesterday);
        System.out.println("标记爽约记录：" + count + " 条");
    }
}