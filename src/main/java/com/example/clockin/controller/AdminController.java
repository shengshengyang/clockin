package com.example.clockin.controller;

import com.example.clockin.model.MenuItem;
import com.example.clockin.model.User;
import com.example.clockin.repo.UserRepository;
import com.example.clockin.service.MenuItemService;
import com.example.clockin.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private MenuItemService menuItemService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = UserUtil.getCurrentUser(userRepository);
        // 根據用戶名查詢更多資訊
        // 加載該用戶角色的菜單項
        List<MenuItem> menuItems = menuItemService.getMenuItemsByRole(user.getRole());
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("user", user);
        return "admin/dashboard";
    }

}
