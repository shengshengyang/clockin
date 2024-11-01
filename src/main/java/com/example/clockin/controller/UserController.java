package com.example.clockin.controller;

import com.example.clockin.model.Shift;
import com.example.clockin.model.User;
import com.example.clockin.repo.ShiftRepository;
import com.example.clockin.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 獲取當前登入用戶的資訊
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);

            if (user == null) {
                response.put("success", false);
                response.put("message", "用戶未找到");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 更新用戶資料
            user.setName(name);
            user.setEmail(email);

            // 如果有上傳頭像，將其轉換為 Base64 字符串
            if (avatarFile != null && !avatarFile.isEmpty()) {
                // 檢查文件大小和類型（例如圖片類型）
                if (avatarFile.getSize() > 5_000_000) { // 限制為5MB
                    response.put("success", false);
                    response.put("message", "文件過大");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                if (!avatarFile.getContentType().startsWith("image/")) {
                    response.put("success", false);
                    response.put("message", "不支持的文件類型");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                String base64Avatar = Base64.getEncoder().encodeToString(avatarFile.getBytes());
                user.setAvatar(base64Avatar);
            }

            // 保存更新後的用戶資料
            userRepository.save(user);

            response.put("success", true);
            response.put("message", "資料更新成功");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "文件處理失敗");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
