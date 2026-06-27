// mapper/StockRecordMapper.java
package com.community.medical_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.medical_service.entity.StockRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockRecordMapper extends BaseMapper<StockRecord> {
    // BaseMapper 已经提供了 insert、delete、update、select 等基础方法
}