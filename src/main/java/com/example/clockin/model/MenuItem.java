package com.example.clockin.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String url;

    @Column(nullable = false, length = 50)
    private String role;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private MenuItem parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> subItems;

    // Getters and Setters
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

    public MenuItem getParent() {
        return parent;
    }

    public void setParent(MenuItem parent) {
        this.parent = parent;
    }

    public List<MenuItem> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<MenuItem> subItems) {
        this.subItems = subItems;
    }
}