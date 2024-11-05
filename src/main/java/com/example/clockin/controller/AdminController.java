package com.example.clockin.controller;

import com.example.clockin.dto.MenuItemDTO;
import com.example.clockin.dto.UserDTO;
import com.example.clockin.model.MenuItem;
import com.example.clockin.model.Shift;
import com.example.clockin.model.User;
import com.example.clockin.repo.ShiftRepository;
import com.example.clockin.repo.UserRepository;
import com.example.clockin.service.AdminService;
import com.example.clockin.service.MenuItemService;
import com.example.clockin.util.UserUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final Logger logger = LogManager.getLogger(AdminController.class);
    @Autowired
    UserRepository userRepository;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private UserUtil util;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private AdminService adminService;


    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        util.addCommonAttributes(model);
        return "admin/dashboard";
    }

    @GetMapping("/user-management")
    public String userManagementPage(Model model) {
        util.addCommonAttributes(model);
        List<Shift> shifts = shiftRepository.findAll();
        model.addAttribute("shifts", shifts);
        return "admin/user-management";
    }

    @GetMapping("/users")
    @ResponseBody
    public List<UserDTO> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll().stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole());
            if (user.getShift() != null) {
                dto.setShiftId(user.getShift().getId());
                dto.setShiftName(user.getShift().getShiftName());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // 新增用戶
    @PostMapping("/users")
    @ResponseBody
    public ResponseEntity<UserDTO> addUser(@RequestBody UserDTO userDetails) {
        logger.info("Adding new user: {}", userDetails.getUsername());
        User user = new User();
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());
        user.setPassword(util.encodePassword("1234")); // Set default password

        if (userDetails.getShiftId() != null) {
            Shift shift = shiftRepository.findById(userDetails.getShiftId())
                    .orElseThrow(() -> new RuntimeException("Shift not found"));
            user.setShift(shift);
        }

        userRepository.save(user);

        UserDTO newUserDTO = adminService.mapToUserDTO(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(newUserDTO);
    }

    // 更新用戶
    @PutMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDetails) {
        logger.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());

        if (userDetails.getShiftId() != null) {
            Shift shift = shiftRepository.findById(userDetails.getShiftId())
                    .orElseThrow(() -> new RuntimeException("Shift not found"));
            user.setShift(shift);
        } else {
            user.setShift(null);
        }
        userRepository.save(user);

        UserDTO updatedUserDTO = adminService.mapToUserDTO(user);

        logger.info("Updated user: {}", user.getUsername());
        return ResponseEntity.ok(updatedUserDTO);
    }

    // 刪除用戶
    @DeleteMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
        logger.info("Deleted user with ID: {}", id);
        return ResponseEntity.noContent().build();
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
        logger.info("Adding new menu item: {}", menuItem.getName());
        menuItemService.saveMenuItem(menuItem);
        return "redirect:/admin/menu/list";
    }

    @PostMapping("/menu/edit")
    public String editMenuItem(@ModelAttribute MenuItem menuItem) {
        logger.info("Editing menu item: {}", menuItem.getId());
        menuItemService.saveMenuItem(menuItem);
        return "redirect:/admin/menu/list";
    }

    @GetMapping("/menu/delete/{id}")
    public String deleteMenuItem(@PathVariable Integer id) {
        logger.info("Deleting menu item with ID: {}", id);
        menuItemService.deleteMenuItemById(id);
        return "redirect:/admin/menu/list";
    }
}
