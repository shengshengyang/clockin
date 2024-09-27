package com.example.clockin.repo;

import com.example.clockin.model.CompanyLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyLocationRepository extends JpaRepository<CompanyLocation, Integer> {
    CompanyLocation findFirstByOrderByIdAsc();

}