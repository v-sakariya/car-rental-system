
package com.example.carrental.dto;

import com.example.carrental.enums.CarType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationRequest(

        @NotNull(message = "Car type is required")
        CarType carType,

        @NotNull(message = "Pickup date and time are required")
        LocalDateTime pickupDateTime,

        @NotNull(message = "Rental duration is required")
        @Min(value = 1, message = "Rental duration must be at least one day")
        Integer numberOfDays

) {
}
