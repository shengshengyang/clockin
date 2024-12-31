package com.example.clockin.config;

import com.example.clockin.util.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
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
                        .requestMatchers("/api/login", "/api/register","api/forgot-password").permitAll()
                        .anyRequest().authenticated() // 其他 /api/** 路徑需要認證
                )
                // 關閉 CSRF (REST API 常見做法)
                // 只忽略 /api/** 的 CSRF
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                // API 通常是「無狀態」，所以關閉 Session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 在 UsernamePasswordAuthenticationFilter 之前放置 JWT filter
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
        ;

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(Constants.LOCAL_FRONT_HOST)); // 允許的來源
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); // 允許的 HTTP 方法
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // 允許的請求頭
        configuration.setAllowCredentials(true); // 是否允許攜帶 Cookie 或憑證

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration); // 將配置應用於 /api/** 路徑
        return source;
    }

}
