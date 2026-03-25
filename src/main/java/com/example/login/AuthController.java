package com.example.login;


import com.example.common.BusinessException;
import com.example.common.Result;
import com.example.config.JwtProperties;
import com.example.login.dto.LoginRequest;
import com.example.login.dto.RegisterRequest;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import com.example.util.JwtUtil;
import com.example.util.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    public Result<Map<String, String>> register(@RequestBody RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return Result.success(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid credentials");
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

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            // ❌ 删除 refreshToken
            redisService.delete("refresh:" + username);
        }

        return Result.success();
    }


    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> body) {

        String refreshToken = body.get("refreshToken");

        if (refreshToken == null) {
            return Result.error(400, "refreshToken不能为空");
        }

        try {
            String username = jwtUtil.extractUsername(refreshToken);

            // ❌ 1. 是否过期
            if (jwtUtil.isTokenExpired(refreshToken)) {
                return Result.error(401, "refreshToken已过期");
            }

            // ❌ 2. Redis 校验（关键）
            String redisToken = redisService.get("refresh:" + username);
            if (redisToken == null || !redisToken.equals(refreshToken)) {
                return Result.error(401, "refreshToken无效");
            }

            // ✅ 3. 生成新 token（rotation）
            String newAccessToken = jwtUtil.generateAccessToken(username);
            String newRefreshToken = jwtUtil.generateRefreshToken(username);

            // ✅ 4. 覆盖 Redis（旧的自动失效）
            redisService.set(
                    "refresh:" + username,
                    newRefreshToken,
                    jwtProperties.getRefreshExpire(),
                    TimeUnit.SECONDS
            );

            Map<String, String> result = new HashMap<>();
            result.put("accessToken", newAccessToken);
            result.put("refreshToken", newRefreshToken);

            return Result.success(result);

        } catch (Exception e) {
            return Result.error(401, "refreshToken解析失败");
        }
    }


}