// 文件位置：src/main/java/com/community/medical_service/mapper/MedicalRecordMapper.java
package com.community.medical_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.medical_service.entity.MedicalRecord;
import com.community.medical_service.vo.MedicalRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {

    @Select("SELECT r.*, " +
            "u.real_name as patient_name, " +
            "d.real_name as doctor_name " +
            "FROM sys_medical_record r " +
            "LEFT JOIN sys_user u ON r.patient_id = u.id " +
            "LEFT JOIN sys_user d ON r.doctor_id = d.id " +
            "WHERE r.patient_id = #{patientId} " +
            "ORDER BY r.visit_date DESC")
    List<MedicalRecordVO> listByPatientId(@Param("patientId") Long patientId);

    /**
     * 🔧 新增：按医生ID查询病历列表
     */
    @Select("SELECT r.*, " +
            "u.real_name as patient_name, " +
            "d.real_name as doctor_name " +
            "FROM sys_medical_record r " +
            "LEFT JOIN sys_user u ON r.patient_id = u.id " +
            "LEFT JOIN sys_user d ON r.doctor_id = d.id " +
            "WHERE r.doctor_id = #{doctorId} " +
            "ORDER BY r.visit_date DESC")
    List<MedicalRecordVO> listByDoctorId(@Param("doctorId") Long doctorId);

    @Select("SELECT r.*, " +
            "u.real_name as patient_name, " +
            "d.real_name as doctor_name " +
            "FROM sys_medical_record r " +
            "LEFT JOIN sys_user u ON r.patient_id = u.id " +
            "LEFT JOIN sys_user d ON r.doctor_id = d.id " +
            "WHERE r.id = #{id}")
    MedicalRecordVO getByIdWithName(@Param("id") Long id);
}