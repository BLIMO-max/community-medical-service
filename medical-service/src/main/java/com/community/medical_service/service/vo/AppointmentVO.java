// vo/AppointmentVO.java
package com.community.medical_service.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentVO {
    private Long id;
    private Long userId;
    private Long scheduleId;
    private Long patientId;
    private Long doctorId;
    private String patientName;
    private String patientGender;
    private Integer patientAge;
    private String patientPhone;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    private Integer timeSlot;
    private String doctorName;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}