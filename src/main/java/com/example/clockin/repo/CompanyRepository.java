package com.example.clockin.repo;

import com.example.clockin.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
    Company findFirstByOrderByIdAsc();

}