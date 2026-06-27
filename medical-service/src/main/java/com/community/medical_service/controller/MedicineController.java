// controller/MedicineController.java
package com.community.medical_service.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.medical_service.common.Result;
import com.community.medical_service.entity.Medicine;
import com.community.medical_service.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/medicine")
//@CrossOrigin
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    /**
     * 药品列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<Medicine>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Medicine> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Medicine> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword).or().like("code", keyword);
        }
        wrapper.orderByAsc("id");
        return Result.success(medicineService.page(page, wrapper));
    }

    /**
     * 库存预警列表
     */
    @GetMapping("/low-stock")
    public Result<List<Medicine>> getLowStock() {
        return Result.success(medicineService.getLowStockList());
    }

    /**
     * 新增药品
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Medicine medicine) {
        boolean success = medicineService.save(medicine);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    /**
     * 更新药品
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Medicine medicine) {
        boolean success = medicineService.updateById(medicine);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除药品
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = medicineService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 入库
     */
    @PostMapping("/in-stock")
    public Result<?> inStock(@RequestParam Long id, @RequestParam Integer quantity,
                             @RequestParam String operator, @RequestParam(required = false) String reason) {
        boolean success = medicineService.addStock(id, quantity, operator, reason);
        return success ? Result.success("入库成功") : Result.error("入库失败");
    }

    /**
     * 出库
     */
    @PostMapping("/out-stock")
    public Result<?> outStock(@RequestParam Long id, @RequestParam Integer quantity,
                              @RequestParam String operator, @RequestParam(required = false) String reason) {
        boolean success = medicineService.reduceStock(id, quantity, operator, reason);
        return success ? Result.success("出库成功") : Result.error("出库失败或库存不足");
    }
}