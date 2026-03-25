package com.example.login.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;
}