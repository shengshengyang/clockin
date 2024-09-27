package com.example.clockin.util;

import org.springframework.beans.factory.annotation.Value;

public class DistanceUtil {
    @Value("company.lat")
    static double companyLat;
    @Value("company.lng")
    static double companyLng;
    public static double calculateDistance(double userLat, double userLng) {

        double earthRadius = 6371000; // 地球半徑，單位：米

        double dLat = Math.toRadians(userLat - companyLat);
        double dLng = Math.toRadians(userLng - companyLng);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(companyLat)) * Math.cos(Math.toRadians(userLat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    public static boolean isWithin200Meters(double userLat, double userLng) {
        return calculateDistance(userLat, userLng) <= 200;
    }
}
