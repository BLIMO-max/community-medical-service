// vo/MedicalRecordVO.java
package com.community.medical_service.vo;

import com.community.medical_service.entity.MedicalRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalRecordVO extends MedicalRecord {
    private String patientName;
    private String patientGender;
    private Integer patientAge;
    private String doctorName;
}