package com.example.config;


import com.example.common.Result;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import com.example.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // 密码加密器
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 核心安全配置
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // ✅ 放行登录/注册
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 无状态（JWT 必须）
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 加 JWT 过滤器
        http.addFilterBefore(new JwtFilter(jwtUtil, userRepository),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * JWT 过滤器（独立类写法，避免 lambda 问题）
     */
    @RequiredArgsConstructor
    public static class JwtFilter extends OncePerRequestFilter {

        private final JwtUtil jwtUtil;
        private final UserRepository userRepository;

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain)
                throws ServletException, IOException {

            final String authHeader = request.getHeader("Authorization");

            String username = null;
            String jwt = null;

            // ✅ 放行登录接口（非常重要）
            String path = request.getRequestURI();
            if (path.contains("/api/auth/login") || path.contains("/api/auth/register")) {
                filterChain.doFilter(request, response);
                return;
            }

            // ❌ 1️⃣ 没带 token → 直接 401
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                write401(response, "未认证，请先登录");
                return;
            }

            // 2️⃣ 解析 token
            try {
                jwt = authHeader.substring(7);
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                write401(response, "token 解析失败");
                return;
            }

            // ❌ 3️⃣ token 无效
            if (username == null) {
                write401(response, "token 无效");
                return;
            }

            // 4️⃣ 校验 token
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByUsername(username).orElse(null);

                if (user == null || !jwtUtil.validateToken(jwt, username)) {
                    write401(response, "token 校验失败");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user, null, null);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            // ✅ 通过，继续执行
            filterChain.doFilter(request, response);
        }

        // ✅ 统一返回 401
        private void write401(HttpServletResponse response, String message) throws IOException {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);

            Result<Void> result = Result.error(401, message);

            response.getWriter().write(
                    new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(result)
            );
        }
    }
}