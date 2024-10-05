package com.example.clockin.service;

import com.example.clockin.model.MenuItem;
import com.example.clockin.repo.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    public List<MenuItem> getMenuItemsByRole(String role) {
        List<MenuItem> parentItems = menuItemRepository.findByRole(role);
        for (MenuItem parent : parentItems) {
            List<MenuItem> children = menuItemRepository.findByParentId(parent.getId());
            parent.setSubItems(children);
        }
        return parentItems;
    }
}