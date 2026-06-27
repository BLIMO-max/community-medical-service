// 文件位置：src/main/java/com/community/medical_service/entity/MedicalRecord.java
package com.community.medical_service.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_medical_record")
public class MedicalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;
    private Long doctorId;
    private String medicalNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    private String complaint;
    private String presentIllness;
    private String pastHistory;
    private String physicalExam;
    private String diagnosis;
    private String treatment;
    private String prescription;
    private String followUp;
    private String notes;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}