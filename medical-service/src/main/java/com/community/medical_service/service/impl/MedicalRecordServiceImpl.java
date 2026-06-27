// service/impl/MedicalRecordServiceImpl.java
package com.community.medical_service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.medical_service.entity.MedicalRecord;
import com.community.medical_service.entity.User;
import com.community.medical_service.mapper.MedicalRecordMapper;
import com.community.medical_service.service.MedicalRecordService;
import com.community.medical_service.service.UserService;
import com.community.medical_service.vo.MedicalRecordVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalRecordServiceImpl
        extends ServiceImpl<MedicalRecordMapper, MedicalRecord>
        implements MedicalRecordService {

    @Autowired
    private UserService userService;

    @Override
    public List<MedicalRecordVO> listByPatientId(Long patientId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getPatientId, patientId)
                .orderByDesc(MedicalRecord::getVisitDate);

        List<MedicalRecord> records = this.list(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicalRecordVO> listByDoctorId(Long doctorId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getDoctorId, doctorId)
                .orderByDesc(MedicalRecord::getVisitDate);

        List<MedicalRecord> records = this.list(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public MedicalRecordVO getByIdWithName(Long id) {
        MedicalRecord record = this.getById(id);
        if (record == null) {
            return null;
        }
        return convertToVO(record);
    }

    @Override
    public boolean createRecord(MedicalRecord record) {
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        if (record.getStatus() == null) {
            record.setStatus(1);
        }
        return this.save(record);
    }

    private MedicalRecordVO convertToVO(MedicalRecord record) {
        MedicalRecordVO vo = new MedicalRecordVO();
        BeanUtils.copyProperties(record, vo);

        if (record.getPatientId() != null) {
            User patient = userService.getById(record.getPatientId());
            if (patient != null) {
                vo.setPatientName(patient.getRealName());
                vo.setPatientGender(patient.getGender());
                vo.setPatientAge(patient.getAge());
            }
        }

        if (record.getDoctorId() != null) {
            User doctor = userService.getById(record.getDoctorId());
            if (doctor != null) {
                vo.setDoctorName(doctor.getRealName());
            }
        }

        return vo;
    }
}