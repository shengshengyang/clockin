package com.example.clockin.controller;

import com.example.clockin.model.Shift;
import com.example.clockin.model.User;
import com.example.clockin.repo.ShiftRepository;
import com.example.clockin.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Controller
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    private ShiftRepository shiftRepository;

    // 顯示用戶個人資料頁面
    @GetMapping("/profile")
    public String profile(Model model) {
        // 獲取當前登入用戶的資訊
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        // 使用 Java DateTimeFormatter 格式化 createdAt
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String formattedCreatedAt = user.getCreatedAt().format(formatter);

        Shift shift = user.getShift();
        model.addAttribute("user", user);
        model.addAttribute("formattedCreatedAt", formattedCreatedAt);
        model.addAttribute("shift", shift);
        return "user/profile";
    }

    // 更新用戶資料，包括頭像
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("name") String name,
                                @RequestParam("email") String email,
                                @RequestParam("avatar") MultipartFile avatarFile) throws IOException {
        // 獲取當前登入用戶的資訊
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        // 更新用戶資料
        user.setName(name);
        user.setEmail(email);

        // 如果有上傳頭像，將其轉換為 Base64 字符串
        if (!avatarFile.isEmpty()) {
            String base64Avatar = Base64.getEncoder().encodeToString(avatarFile.getBytes());
            user.setAvatar(base64Avatar);
        }

        // 保存更新後的用戶資料
        userRepository.save(user);

        return "redirect:/user/profile";
    }
}
