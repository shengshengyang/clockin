package com.example.clockin.util;

import com.example.clockin.model.MenuItem;
import com.example.clockin.model.User;
import com.example.clockin.repo.UserRepository;
import com.example.clockin.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.List;

@Component
public class UserUtil {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuItemService menuItemService;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username);
    }

    public void addCommonAttributes(Model model) {
        User user = getCurrentUser();
        List<MenuItem> menuItems = menuItemService.getMenuItemsByRole(user.getRole());
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("user", user);
    }

    // password encoder
    public String encodePassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }
}
