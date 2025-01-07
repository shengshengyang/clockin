package com.example.clockin.dto;

import com.example.clockin.model.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemDTO {
    private Integer id;
    private String name;
    private String url;
    private String role;
    private String parentName;

}
