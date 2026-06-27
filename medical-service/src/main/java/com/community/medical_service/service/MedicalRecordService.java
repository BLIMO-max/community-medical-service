// service/MedicalRecordService.java
package com.community.medical_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.medical_service.entity.MedicalRecord;
import com.community.medical_service.vo.MedicalRecordVO;
import java.util.List;

public interface MedicalRecordService extends IService<MedicalRecord> {
    List<MedicalRecordVO> listByPatientId(Long patientId);
    List<MedicalRecordVO> listByDoctorId(Long doctorId);
    MedicalRecordVO getByIdWithName(Long id);
    boolean createRecord(MedicalRecord record);
}