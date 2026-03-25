package com.example.login.dto;


import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
}