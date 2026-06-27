// mapper/MedicineMapper.java
package com.community.medical_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.medical_service.entity.Medicine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface MedicineMapper extends BaseMapper<Medicine> {

    @Update("UPDATE sys_medicine SET stock = stock + #{quantity} WHERE id = #{id}")
    int addStock(@org.apache.ibatis.annotations.Param("id") Long id,
                 @org.apache.ibatis.annotations.Param("quantity") Integer quantity);

    @Update("UPDATE sys_medicine SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}")
    int reduceStock(@org.apache.ibatis.annotations.Param("id") Long id,
                    @org.apache.ibatis.annotations.Param("quantity") Integer quantity);

    @Select("SELECT * FROM sys_medicine WHERE stock <= min_stock")
    List<Medicine> getLowStockList();
}