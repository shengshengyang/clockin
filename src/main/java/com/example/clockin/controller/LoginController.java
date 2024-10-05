package com.example.clockin.controller;

import com.example.clockin.model.MenuItem;
import com.example.clockin.model.User;
import com.example.clockin.repo.MenuItemRepository;
import com.example.clockin.repo.UserRepository;
import com.example.clockin.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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