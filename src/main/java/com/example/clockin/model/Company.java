package com.example.clockin.model;

import com.example.clockin.config.AuditEntityListener;
import com.example.clockin.service.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "company")
@EntityListeners(AuditEntityListener.class)
public class Company implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double radius;

    @Transient
    private String oldValueSnapshot;
}
