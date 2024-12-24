package com.example.clockin.config;

import com.example.clockin.util.CustomAuthenticationSuccessHandler;
import com.example.clockin.util.CustomLogoutSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@Order(2)  // 排在 ApiSecurityConfig 後面
public class WebSecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomLogoutSuccessHandler logoutSuccessHandler;

    // 這裡不再 @Autowired PasswordEncoder、UserDetailsService，
    // 而是透過下方的 @Bean authenticationProvider(...) 來注入。

    public WebSecurityConfig(
            CustomAuthenticationSuccessHandler successHandler,
            CustomLogoutSuccessHandler logoutSuccessHandler
    ) {
        this.successHandler = successHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    /**
     * 建立 DaoAuthenticationProvider，
     * 需要從容器中自動注入 PasswordEncoder 與 UserDetailsService
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
            org.springframework.security.core.userdetails.UserDetailsService userDetailsService
    ) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain webSecurityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider
    ) throws Exception {

        http
                // 使用我們自定義的 DaoAuthenticationProvider
                .authenticationProvider(authenticationProvider)
                // 設定路徑與權限
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/attendance/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/assets/**").permitAll()
                        .anyRequest().permitAll()
                )
                // 表單登入
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                // 登出
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .permitAll()
                )
                // 異常處理 (未認證 / 無權限)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/login");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/403");
                        })
                );

        return http.build();
    }
}
