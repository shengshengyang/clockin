package com.example.clockin.controller;

import com.example.clockin.dto.MenuItemDTO;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private UserUtil util;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        util.addCommonAttributes(model);
        return "admin/dashboard";
    }

    @GetMapping("/menu/list/page")
    public String listMenuItemsPage(Model model) {
        util.addCommonAttributes(model);
        User user = util.getCurrentUser();
        List<MenuItem> menuItems = menuItemService.getMenuItemsByRole(user.getRole());
        model.addAttribute("menuItems", menuItems);
        return "admin/menu-list";
    }

    @GetMapping("/menu/list")
    @ResponseBody
    public List<MenuItemDTO> listMenuItems() {
        List<MenuItem> menuItems = menuItemService.getAllMenuItems();
        return menuItems.stream().map(MenuItemDTO::new).collect(Collectors.toList());
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
