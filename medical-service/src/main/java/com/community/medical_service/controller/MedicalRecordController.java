// controller/MedicalRecordController.java
package com.community.medical_service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.medical_service.common.Result;
import com.community.medical_service.entity.MedicalRecord;
import com.community.medical_service.service.MedicalRecordService;
import com.community.medical_service.vo.MedicalRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/medical-record")
//@CrossOrigin
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    /**
     * 获取患者的病历列表
     */
    @GetMapping("/patient/{patientId}")
    public Result<List<MedicalRecordVO>> listByPatientId(@PathVariable Long patientId) {
        List<MedicalRecordVO> list = medicalRecordService.listByPatientId(patientId);
        return Result.success(list);
    }
    /**
     * 获取医生的诊断记录列表
     */
    @GetMapping("/doctor/{doctorId}")
    public Result<List<MedicalRecordVO>> listByDoctorId(@PathVariable Long doctorId) {
        List<MedicalRecordVO> list = medicalRecordService.listByDoctorId(doctorId);
        return Result.success(list);
    }
    /**
     * 获取病历详情
     */
    @GetMapping("/{id}")
    public Result<MedicalRecordVO> getById(@PathVariable Long id) {
        MedicalRecordVO record = medicalRecordService.getByIdWithName(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.error("病历不存在");
    }
    /**
     * 创建病历（医生端）
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody MedicalRecord record) {
        boolean success = medicalRecordService.createRecord(record);
        if (success) {
            return Result.success("病历创建成功");
        }
        return Result.error("创建失败");
    }

    /**
     * 更新病历
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody MedicalRecord record) {
        boolean success = medicalRecordService.updateById(record);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除病历
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = medicalRecordService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}