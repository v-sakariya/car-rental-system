package com.example.carrental.dto;

import com.example.carrental.enums.CarType;
import com.example.carrental.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID reservationId,
        UUID carId,
        CarType carType,
        LocalDateTime pickupDateTime,
        LocalDateTime returnDateTime,
        ReservationStatus status
) {
}