// 文件位置：src/main/java/com/community/medical_service/controller/UserController.java
package com.community.medical_service.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.medical_service.common.Result;
import com.community.medical_service.entity.User;
import com.community.medical_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;      // 🔧 添加这个导入
import java.util.stream.Collectors;  // 🔧 添加这个导入

@RestController
@RequestMapping("/user")
//@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return Result.error("用户名或密码不能为空");
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        wrapper.eq("password", user.getPassword());
        User dbUser = userService.getOne(wrapper);

        if (dbUser != null) {
            dbUser.setPassword(null);
            return Result.success(dbUser);
        } else {
            return Result.error("用户名或密码错误");
        }
    }
    /**
     * 注册接口
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (userService.count(wrapper) > 0) {
            return Result.error("用户名已存在，请更换");
        }

        if (user.getRole() == null) {
            user.setRole(2);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        // 不设置时间，让数据库自动生成
        boolean success = userService.save(user);
        if (success) {
            return Result.success("注册成功");
        } else {
            return Result.error("注册失败，请稍后重试");
        }
    }

    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role) {
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("username", keyword)
                    .or().like("real_name", keyword)
                    .or().like("phone", keyword));
        }
        if (role != null) {
            wrapper.eq("role", role);
        }
        wrapper.orderByDesc("id");

        return Result.success(userService.page(page, wrapper));
    }

    /**
     * 新增用户
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (userService.count(wrapper) > 0) {
            return Result.error("用户名已存在");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 不设置 createTime 和 updateTime，让数据库自动生成

        boolean success = userService.save(user);
        if (success) {
            return Result.success("添加成功");
        } else {
            return Result.error("添加失败");
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody User user) {
        // 密码不更新
        user.setPassword(null);

        boolean success = userService.updateById(user);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/status/{id}")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        boolean success = userService.updateById(user);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = userService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 重置密码
     */
    @PutMapping("/reset-pwd/{id}")
    public Result<?> resetPassword(@PathVariable Long id) {
        User user = new User();
        user.setId(id);
        user.setPassword("123456");
        boolean success = userService.updateById(user);
        return success ? Result.success("密码已重置为123456") : Result.error("重置失败");
    }

    /**
     * 获取医生列表
     */
    @GetMapping("/doctors")
    public Result<?> getDoctors() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", 1);      // 角色为医生
        wrapper.eq("status", 1);    // 状态正常
        wrapper.select("id", "real_name as realName", "phone");
        List<User> doctors = userService.list(wrapper);
        return Result.success(doctors);
    }
}