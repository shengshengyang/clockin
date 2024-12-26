package com.example.clockin.config;

import com.example.clockin.util.Constants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CrossOriginsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // 只對 /api/** 路徑開放 CORS
                .allowedOrigins(Constants.LOCAL_FRONT_HOST)  // 允許來自 http://localhost:9000 的請求
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // 允許的 HTTP 方法
                .allowedHeaders("*")  // 允許所有的請求頭
                .allowCredentials(true);  // 允許攜帶憑證
    }
}
