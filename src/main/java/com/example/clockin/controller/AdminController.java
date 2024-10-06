package com.example.clockin.controller;

import com.example.clockin.model.MenuItem;
import com.example.clockin.model.User;
import com.example.clockin.repo.UserRepository;
import com.example.clockin.service.MenuItemService;
import com.example.clockin.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/menu/list/page")
    public String listMenuItemsPage() {
        return "admin/menu-list";
    }

    @GetMapping("/menu/list")
    @ResponseBody
    public List<MenuItem> listMenuItems() {
        return menuItemService.getAllMenuItems();
    }


    @PostMapping("/menu/add")
    public String addMenuItem(@ModelAttribute MenuItem menuItem) {
        menuItemService.saveMenuItem(menuItem);
        return "redirect:/admin/menu/list";
    }

    @PostMapping("/menu/edit")
    public String editMenuItem(@ModelAttribute MenuItem menuItem) {
        menuItemService.saveMenuItem(menuItem);
        return "redirect:/admin/menu/list";
    }

    @GetMapping("/menu/delete/{id}")
    public String deleteMenuItem(@PathVariable Integer id) {
        menuItemService.deleteMenuItemById(id);
        return "redirect:/admin/menu/list";
    }
}
