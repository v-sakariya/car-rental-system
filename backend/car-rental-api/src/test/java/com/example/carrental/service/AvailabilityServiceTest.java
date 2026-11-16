
package com.example.carrental.service;

import com.example.carrental.dto.AvailabilityResponse;
import com.example.carrental.entity.Car;
import com.example.carrental.enums.CarType;
import com.example.carrental.enums.ReservationStatus;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.ReservationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private LocalDateTime pickup;

    @BeforeEach
    void setUp() {
        pickup = LocalDateTime.now().plusDays(10);
    }

    @Test
    void shouldReturnCorrectAvailableVehicleCount() {

        Car firstCar = mock(Car.class);
        Car secondCar = mock(Car.class);
        Car thirdCar = mock(Car.class);

        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        UUID thirdId = UUID.randomUUID();

        when(firstCar.getId()).thenReturn(firstId);
        when(secondCar.getId()).thenReturn(secondId);
        when(thirdCar.getId()).thenReturn(thirdId);

        when(carRepository.findByTypeAndActiveTrue(CarType.SEDAN))
                .thenReturn(List.of(firstCar, secondCar, thirdCar));

        LocalDateTime returnDate = pickup.plusDays(3);

        when(reservationRepository.countOverlappingReservations(
                eq(firstId), eq(pickup), eq(returnDate),
                eq(ReservationStatus.CONFIRMED)))
                .thenReturn(1L);

        when(reservationRepository.countOverlappingReservations(
                eq(secondId), eq(pickup), eq(returnDate),
                eq(ReservationStatus.CONFIRMED)))
                .thenReturn(0L);

        when(reservationRepository.countOverlappingReservations(
                eq(thirdId), eq(pickup), eq(returnDate),
                eq(ReservationStatus.CONFIRMED)))
                .thenReturn(0L);

        AvailabilityResponse response =
                availabilityService.checkAvailability(
                        CarType.SEDAN, pickup, 3
                );

        assertEquals(CarType.SEDAN, response.carType());
        assertEquals(pickup, response.pickupDateTime());
        assertEquals(returnDate, response.returnDateTime());
        assertEquals(3, response.totalVehicles());
        assertEquals(2, response.availableVehicles());
    }

    @Test
    void shouldReturnZeroWhenAllVehiclesAreReserved() {

        Car car = mock(Car.class);
        UUID carId = UUID.randomUUID();

        when(car.getId()).thenReturn(carId);

        when(carRepository.findByTypeAndActiveTrue(CarType.SEDAN))
                .thenReturn(List.of(car));

        when(reservationRepository.countOverlappingReservations(
                eq(carId),
                eq(pickup),
                eq(pickup.plusDays(2)),
                eq(ReservationStatus.CONFIRMED)))
                .thenReturn(1L);

        AvailabilityResponse response =
                availabilityService.checkAvailability(
                        CarType.SEDAN, pickup, 2
                );

        assertEquals(1, response.totalVehicles());
        assertEquals(0, response.availableVehicles());
    }

    @Test
    void shouldReturnZeroWhenNoVehiclesExist() {

        when(carRepository.findByTypeAndActiveTrue(CarType.VAN))
                .thenReturn(List.of());

        AvailabilityResponse response =
                availabilityService.checkAvailability(
                        CarType.VAN, pickup, 2
                );

        assertEquals(0, response.totalVehicles());
        assertEquals(0, response.availableVehicles());

        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldRejectPastPickupDate() {

        assertThrows(
                IllegalArgumentException.class,
                () -> availabilityService.checkAvailability(
                        CarType.SEDAN,
                        LocalDateTime.now().minusDays(1),
                        2
                )
        );

        verifyNoInteractions(carRepository);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldRejectInvalidRentalDuration() {

        assertThrows(
                IllegalArgumentException.class,
                () -> availabilityService.checkAvailability(
                        CarType.SEDAN, pickup, 0
                )
        );

        verifyNoInteractions(carRepository);
        verifyNoInteractions(reservationRepository);
    }
}
