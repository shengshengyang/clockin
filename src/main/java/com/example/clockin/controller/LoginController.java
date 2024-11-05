package com.example.clockin.controller;

import com.example.clockin.repo.MenuItemRepository;
import com.example.clockin.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    MenuItemRepository menuItemRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // 對應於 templates/login.html
    }

}