package com.example.clockin.dto;

import com.example.clockin.model.MenuItem;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {
    private Integer id;
    private String name;
    private String url;
    private String role;
    private String parentName;

    public MenuItemDTO(MenuItem menuItem) {
    }
}
