package com.example.login.service;

import com.example.common.BusinessException;
import com.example.common.Result;
import com.example.config.JwtProperties;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import com.example.util.JwtUtil;
import com.example.util.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final JwtProperties jwtProperties;


    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Result<Map<String, String>> login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // ✅ 存 refreshToken
        redisService.set(
                "refresh:" + user.getUsername(),
                refreshToken,
                jwtProperties.getRefreshExpire(),
                TimeUnit.SECONDS
        );

        return Result.success(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
    }
}
