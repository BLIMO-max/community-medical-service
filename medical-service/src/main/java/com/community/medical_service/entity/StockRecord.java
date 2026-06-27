// entity/StockRecord.java
package com.community.medical_service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_stock_record")
public class StockRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long medicineId;    // 药品ID
    private Integer type;       // 1:入库 2:出库
    private Integer quantity;   // 数量
    private Double totalAmount; // 总金额
    private String operator;    // 操作人
    private String reason;      // 原因/备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}