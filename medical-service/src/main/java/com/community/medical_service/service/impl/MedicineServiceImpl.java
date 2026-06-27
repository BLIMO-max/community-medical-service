// service/impl/MedicineServiceImpl.java
package com.community.medical_service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.medical_service.entity.Medicine;
import com.community.medical_service.entity.StockRecord;
import com.community.medical_service.mapper.MedicineMapper;
import com.community.medical_service.mapper.StockRecordMapper;
import com.community.medical_service.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MedicineServiceImpl extends ServiceImpl<MedicineMapper, Medicine> implements MedicineService {

    @Autowired
    private MedicineMapper medicineMapper;

    @Autowired
    private StockRecordMapper stockRecordMapper;

    @Override
    @Transactional
    public boolean addStock(Long medicineId, Integer quantity, String operator, String reason) {
        int rows = medicineMapper.addStock(medicineId, quantity);
        if (rows > 0) {
            StockRecord record = new StockRecord();
            record.setMedicineId(medicineId);
            record.setType(1);
            record.setQuantity(quantity);
            record.setOperator(operator);
            record.setReason(reason);
            stockRecordMapper.insert(record);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean reduceStock(Long medicineId, Integer quantity, String operator, String reason) {
        int rows = medicineMapper.reduceStock(medicineId, quantity);
        if (rows > 0) {
            StockRecord record = new StockRecord();
            record.setMedicineId(medicineId);
            record.setType(2);
            record.setQuantity(quantity);
            record.setOperator(operator);
            record.setReason(reason);
            stockRecordMapper.insert(record);
            return true;
        }
        return false;
    }

    @Override
    public List<Medicine> getLowStockList() {
        return medicineMapper.getLowStockList();
    }

    @Override
    public void saveStockRecord(StockRecord record) {
        stockRecordMapper.insert(record);
    }
}