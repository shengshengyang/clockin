package com.example.clockin.repo;

import com.example.clockin.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    List<MenuItem> findByRole(String role);

    List<MenuItem> findByParentId(Integer parentId);
}