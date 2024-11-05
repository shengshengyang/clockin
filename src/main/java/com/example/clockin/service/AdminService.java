package com.example.clockin.service;

import com.example.clockin.dto.UserDTO;
import com.example.clockin.model.User;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    public UserDTO mapToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());
        if (user.getShift() != null) {
            userDTO.setShiftId(user.getShift().getId());
            userDTO.setShiftName(user.getShift().getShiftName());
        }
        return userDTO;
    }
}
