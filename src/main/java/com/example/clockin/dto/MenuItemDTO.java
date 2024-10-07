package com.example.clockin.dto;

import com.example.clockin.model.MenuItem;

public class MenuItemDTO {
    private Integer id;
    private String name;
    private String url;
    private String role;
    private String parentName;

    public MenuItemDTO() {
    }

    public MenuItemDTO(MenuItem menuItem) {
        this.id = menuItem.getId();
        this.name = menuItem.getName();
        this.url = menuItem.getUrl();
        this.role = menuItem.getRole();
        this.parentName = menuItem.getParent() != null ? menuItem.getParent().getName() : null;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
}
