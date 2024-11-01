package com.example.clockin.util;

import com.example.clockin.model.MenuItem;
import com.example.clockin.model.Shift;
import com.example.clockin.model.ShiftPeriod;
import com.example.clockin.model.User;
import com.example.clockin.repo.MenuItemRepository;
import com.example.clockin.repo.ShiftPeriodRepository;
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

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private ShiftPeriodRepository shiftPeriodRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataInitializer is running...");

// 檢查並創建班別
        Shift splitShift = shiftRepository.findByShiftName("兩頭班");
        if (splitShift == null) {
            splitShift = new Shift();
            splitShift.setShiftName("兩頭班");
            shiftRepository.save(splitShift);

            // 創建早班段（5:00 - 9:00）
            ShiftPeriod morningPeriod = new ShiftPeriod();
            morningPeriod.setShift(splitShift);
            morningPeriod.setStartTime(java.time.LocalTime.of(5, 0));  // 05:00
            morningPeriod.setEndTime(java.time.LocalTime.of(9, 0));    // 09:00
            morningPeriod.setAllowedLateMinutes(10);  // 允許遲到10分鐘
            morningPeriod.setAllowedEarlyLeaveMinutes(5); // 允許早退5分鐘
            shiftPeriodRepository.save(morningPeriod);

            // 創建晚班段（17:00 - 20:00）
            ShiftPeriod eveningPeriod = new ShiftPeriod();
            eveningPeriod.setShift(splitShift);
            eveningPeriod.setStartTime(java.time.LocalTime.of(17, 0)); // 17:00
            eveningPeriod.setEndTime(java.time.LocalTime.of(20, 0));   // 20:00
            eveningPeriod.setAllowedLateMinutes(10);  // 允許遲到10分鐘
            eveningPeriod.setAllowedEarlyLeaveMinutes(5); // 允許早退5分鐘
            shiftPeriodRepository.save(eveningPeriod);
        }

// 檢查並創建普通早班
        Shift morningShift = shiftRepository.findByShiftName("早班");
        if (morningShift == null) {
            morningShift = new Shift();
            morningShift.setShiftName("早班");
            shiftRepository.save(morningShift);

            ShiftPeriod standardMorningPeriod = new ShiftPeriod();
            standardMorningPeriod.setShift(morningShift);
            standardMorningPeriod.setStartTime(java.time.LocalTime.of(9, 0));  // 09:00
            standardMorningPeriod.setEndTime(java.time.LocalTime.of(18, 0));   // 18:00
            standardMorningPeriod.setAllowedLateMinutes(15);  // 允許遲到15分鐘
            standardMorningPeriod.setAllowedEarlyLeaveMinutes(10); // 允許早退10分鐘
            shiftPeriodRepository.save(standardMorningPeriod);
        }

// 檢查並創建普通中班
        Shift afternoonShift = shiftRepository.findByShiftName("中班");
        if (afternoonShift == null) {
            afternoonShift = new Shift();
            afternoonShift.setShiftName("中班");
            shiftRepository.save(afternoonShift);

            ShiftPeriod standardAfternoonPeriod = new ShiftPeriod();
            standardAfternoonPeriod.setShift(afternoonShift);
            standardAfternoonPeriod.setStartTime(java.time.LocalTime.of(14, 0));  // 14:00
            standardAfternoonPeriod.setEndTime(java.time.LocalTime.of(22, 0));    // 22:00
            standardAfternoonPeriod.setAllowedLateMinutes(10);  // 允許遲到10分鐘
            standardAfternoonPeriod.setAllowedEarlyLeaveMinutes(5); // 允許早退5分鐘
            shiftPeriodRepository.save(standardAfternoonPeriod);
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

        if (menuItemRepository.count() == 0) {
            MenuItem admin = new MenuItem();
            admin.setName("Admin");
            admin.setUrl("#");
            admin.setRole("ADMIN");
            menuItemRepository.save(admin);

            MenuItem adminDashboard = new MenuItem();
            adminDashboard.setName("Admin Dashboard");
            adminDashboard.setUrl("/admin/dashboard");
            adminDashboard.setRole("ADMIN");
            adminDashboard.setParent(admin);
            menuItemRepository.save(adminDashboard);

            MenuItem manageUsers = new MenuItem();
            manageUsers.setName("Manage Users");
            manageUsers.setUrl("/admin/manage-users");
            manageUsers.setRole("ADMIN");
            manageUsers.setParent(admin);
            menuItemRepository.save(manageUsers);

            MenuItem user = new MenuItem();
            user.setName("User");
            user.setUrl("#");
            user.setRole("USER");
            menuItemRepository.save(user);

            MenuItem userClockIn = new MenuItem();
            userClockIn.setName("User Clock In");
            userClockIn.setUrl("/user/clockin");
            userClockIn.setRole("USER");
            userClockIn.setParent(user);
            menuItemRepository.save(userClockIn);
        }

        System.out.println("Data initialization completed.");
    }
}
