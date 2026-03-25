package com.example.common;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result<T> {

    private Integer code;   // 状态码
    private String message; // 提示信息
    private T data;         // 数据

    // 成功
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    // 成功（无数据）
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(200)
                .message("success")
                .data(null)
                .build();
    }

    // 失败
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }


}