package com.example.clockin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // 返回自定義的登入頁面
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // 對應於 templates/login.html
    }

    // Spring Security 自動處理登出，不需要在控制器中明確定義登出方法
}