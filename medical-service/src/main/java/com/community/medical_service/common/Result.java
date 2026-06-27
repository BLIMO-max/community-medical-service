// 文件位置：src/main/java/com/community/medical_service/common/Result.java
package com.community.medical_service.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 200:成功, 400:参数错误, 401:未授权, 500:失败
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "操作成功";
        r.data = data;
        return r;
    }

    public static <T> Result<T> success(String msg) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = msg;
        return r;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.msg = msg;
        return r;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        return r;
    }
}