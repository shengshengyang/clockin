package com.example.clockin.util;

import com.example.clockin.model.Shift;
import com.example.clockin.model.User;
import com.example.clockin.repo.ShiftRepository;
import com.example.clockin.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataInitializer is running...");

        // 檢查並創建班別
        Shift morningShift = shiftRepository.findByShiftName("早班");
        if (morningShift == null) {
            morningShift = new Shift();
            morningShift.setShiftName("早班");
            morningShift.setStartTime(java.time.LocalTime.of(9, 0));  // 09:00
            morningShift.setEndTime(java.time.LocalTime.of(18, 0));   // 18:00
            shiftRepository.save(morningShift);
        }

        Shift afternoonShift = shiftRepository.findByShiftName("中班");
        if (afternoonShift == null) {
            afternoonShift = new Shift();
            afternoonShift.setShiftName("中班");
            afternoonShift.setStartTime(java.time.LocalTime.of(14, 0));  // 14:00
            afternoonShift.setEndTime(java.time.LocalTime.of(22, 0));    // 22:00
            shiftRepository.save(afternoonShift);
        }

        // 創建並保存用戶，並分配班別
        if (userRepository.findByUsername("user1") == null) {
            User user1 = new User();
            user1.setUsername("user1");
            user1.setName("測試使用者");
            user1.setPassword(passwordEncoder.encode("password1"));
            user1.setRole("USER");
            user1.setShift(morningShift);  // 分配早班
            user1.setEmail("user@gmail.com");
            userRepository.save(user1);
        }

        if (userRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setName("管理員");
            admin.setPassword(passwordEncoder.encode("password2"));
            admin.setRole("ADMIN");
            admin.setEmail("admin@gmail.com");
            admin.setShift(afternoonShift);  // 分配中班
            userRepository.save(admin);
        }

        System.out.println("Data initialization completed.");
    }
}
