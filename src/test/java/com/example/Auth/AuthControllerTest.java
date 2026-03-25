package com.example.Auth;

import com.example.config.JwtProperties;
import com.example.login.AuthController;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import com.example.util.JwtUtil;
import com.example.util.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockitoBean
    private RedisService redisService;

    @MockitoBean
    private JwtProperties jwtProperties;

    /**
     * ✅ 登录成功
     */
    @Test
    void login_success() throws Exception {

        User user = new User();
        user.setUsername("zwx");
        user.setPassword("encoded");

        when(userRepository.findByUsername("zwx"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(true);

        when(jwtUtil.generateAccessToken(any()))
                .thenReturn("access-token");

        when(jwtUtil.generateRefreshToken(any()))
                .thenReturn("refresh-token");

        doNothing().when(redisService)
                .set(any(), any(), anyLong(), any());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "zwx",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    /**
     * ❌ 用户不存在
     */
    @Test
    void login_fail_user_not_found() throws Exception {

        when(userRepository.findByUsername("zwx"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "zwx",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().is5xxServerError());
    }

    /**
     * ❌ 密码错误
     */
    @Test
    void login_fail_wrong_password() throws Exception {

        User user = new User();
        user.setUsername("zwx");
        user.setPassword("encoded");

        when(userRepository.findByUsername("zwx"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "zwx",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().is5xxServerError());
    }
}