package com.example.clockin.controller;

import com.example.clockin.model.User;
import com.example.clockin.repo.UserRepository;
import com.example.clockin.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserRepository userRepository;
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = UserUtil.getCurrentUser(userRepository);
        // 根據用戶名查詢更多資訊
        model.addAttribute("user", user);
        return "admin/dashboard";
    }

}
