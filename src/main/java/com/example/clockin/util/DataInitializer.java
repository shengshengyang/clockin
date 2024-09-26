package com.example.clockin.util;

import com.example.clockin.model.CompanyLocation;
import com.example.clockin.model.User;
import com.example.clockin.repo.CompanyLocationRepository;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CompanyLocationRepository companyLocationRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataInitializer is running...");
        if (userRepository.findByUsername("user1") == null) {
            User user1 = new User();
            user1.setUsername("user1");
            user1.setPassword(passwordEncoder.encode("password1")); // 使用 PasswordEncoder 加密密码
            user1.setRole("USER");
            userRepository.save(user1);
        }

        if (userRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("password2")); // 使用 PasswordEncoder 加密密码
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }

        if (companyLocationRepository.count() == 0) {
            CompanyLocation companyLocation = new CompanyLocation();
            companyLocation.setName("總部");
            companyLocation.setLatitude(25.033964);
            companyLocation.setLongitude(121.564468);
            companyLocationRepository.save(companyLocation);
        }
        System.out.println("Data initialization completed.");
    }
}