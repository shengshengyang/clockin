package com.example.clockin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClockInEvent implements Serializable {

    private String username;
    private double latitude;
    private double longitude;

}
