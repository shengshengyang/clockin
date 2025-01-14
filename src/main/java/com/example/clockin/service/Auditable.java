package com.example.clockin.service;

public interface Auditable {
    String getOldValueSnapshot();
    void setOldValueSnapshot(String oldValueSnapshot);
}