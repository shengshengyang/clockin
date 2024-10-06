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

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public MenuItem getMenuItemById(Integer id) {
        return menuItemRepository.findById(id).orElse(null);
    }

    public void saveMenuItem(MenuItem menuItem) {
        menuItemRepository.save(menuItem);
    }

    public void deleteMenuItemById(Integer id) {
        menuItemRepository.deleteById(id);
    }
}