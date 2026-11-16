
package com.example.carrental.controller;

import com.example.carrental.dto.AvailabilityResponse;
import com.example.carrental.enums.CarType;
import com.example.carrental.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/cars")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(
            AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/availability")
    public AvailabilityResponse checkAvailability(
            @RequestParam CarType carType,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime pickupDateTime,

            @RequestParam int numberOfDays) {

        return availabilityService.checkAvailability(
                carType,
                pickupDateTime,
                numberOfDays
        );
    }
}
