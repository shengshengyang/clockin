package com.example.clockin.service.factory;

import com.example.clockin.dto.ClockInEvent;
import com.example.clockin.model.Company;
import com.example.clockin.repo.CompanyRepository;
import com.example.clockin.service.ClockInService;
import org.springframework.stereotype.Component;

@Component
public class ClockInFactory {

    ClockInService clockInService;
    CompanyRepository companyRepository;

    public ClockInFactory(ClockInService clockInService, CompanyRepository companyRepository) {

        this.clockInService = clockInService;
        this.companyRepository = companyRepository;
    }


    public ClockInEvent createClockInEvent(String username, double lat, double lng) {


        Company company = companyRepository.findFirstByOrderByIdAsc();

        double distance = clockInService.calculateDistance(lat, lng, company.getLatitude(), company.getLongitude());
        if (distance > 200) {
            throw new IllegalArgumentException("The distance between the provided coordinates and the clock-in coordinates must not exceed 200 units.");
        }
        // 這裡只是簡單示範
        return new ClockInEvent(username, lat, lng);
    }

}