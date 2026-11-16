
package com.example.carrental.service;

import com.example.carrental.dto.AvailabilityResponse;
import com.example.carrental.entity.Car;
import com.example.carrental.enums.CarType;
import com.example.carrental.enums.ReservationStatus;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvailabilityService {

    private final CarRepository carRepository;
    private final ReservationRepository reservationRepository;

    public AvailabilityService(
            CarRepository carRepository,
            ReservationRepository reservationRepository) {
        this.carRepository = carRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(
            CarType carType,
            LocalDateTime pickupDateTime,
            int numberOfDays) {

        validateRequest(pickupDateTime, numberOfDays);

        LocalDateTime returnDateTime =
                pickupDateTime.plusDays(numberOfDays);

        List<Car> cars =
                carRepository.findByTypeAndActiveTrue(carType);

        int availableCount = 0;

        for (Car car : cars) {

            long overlappingReservations =
                    reservationRepository.countOverlappingReservations(
                            car.getId(),
                            pickupDateTime,
                            returnDateTime,
                            ReservationStatus.CONFIRMED
                    );

            if (overlappingReservations == 0) {
                availableCount++;
            }
        }

        return new AvailabilityResponse(
                carType,
                pickupDateTime,
                returnDateTime,
                cars.size(),
                availableCount
        );
    }

    private void validateRequest(
            LocalDateTime pickupDateTime,
            int numberOfDays) {

        if (pickupDateTime == null ||
                !pickupDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Pickup date and time must be in the future"
            );
        }

        if (numberOfDays <= 0) {
            throw new IllegalArgumentException(
                    "Rental duration must be at least one day"
            );
        }
    }
}
