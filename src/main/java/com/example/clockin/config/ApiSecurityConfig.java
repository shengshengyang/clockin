package com.example.clockin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Order(1)  // 設定優先處理此組 FilterChain
public class ApiSecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public ApiSecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 只攔截 /api/** 路徑 (必須符合此 matcher 才會進入此 FilterChain)
                .securityMatcher("/api/**")

                .authorizeHttpRequests(auth -> auth
                        // 如 /api/login, /api/register 不需帶 JWT
                        .requestMatchers("/api/login", "/api/register").permitAll()
                        // 其餘 /api/** 都要驗證
                        .anyRequest().authenticated()
                )
                // API 通常是「無狀態」，所以關閉 Session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // API 常用 RESTful 方式，通常也會關閉 CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // 在 UsernamePasswordAuthenticationFilter 之前放置 JWT filter
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
