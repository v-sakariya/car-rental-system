package com.example.carrental.dto;

import com.example.carrental.enums.CarType;

import java.time.LocalDateTime;

public record AvailabilityResponse(
        CarType carType,
        LocalDateTime pickupDateTime,
        LocalDateTime returnDateTime,
        int totalVehicles,
        int availableVehicles
) {
}