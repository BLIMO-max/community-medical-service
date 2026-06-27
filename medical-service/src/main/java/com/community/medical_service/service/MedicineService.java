// service/MedicineService.java
package com.community.medical_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.medical_service.entity.Medicine;
import com.community.medical_service.entity.StockRecord;
import java.util.List;

public interface MedicineService extends IService<Medicine> {
    boolean addStock(Long medicineId, Integer quantity, String operator, String reason);
    boolean reduceStock(Long medicineId, Integer quantity, String operator, String reason);
    List<Medicine> getLowStockList();
    void saveStockRecord(StockRecord record);
}