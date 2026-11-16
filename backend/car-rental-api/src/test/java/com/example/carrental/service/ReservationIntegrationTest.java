
package com.example.carrental.service;

import com.example.carrental.dto.ReservationRequest;
import com.example.carrental.entity.Car;
import com.example.carrental.enums.CarType;
import com.example.carrental.exception.CarNotAvailableException;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.ReservationRepository;
import com.example.carrental.enums.ReservationStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReservationIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        // Remove existing reservations before deleting vehicles.
        reservationRepository.deleteAllInBatch();
        carRepository.deleteAllInBatch();
    }

    @Test
    void shouldAssignDifferentCarsForOverlappingReservations() {

        carRepository.saveAndFlush(new Car(CarType.SEDAN));
        carRepository.saveAndFlush(new Car(CarType.SEDAN));

        LocalDateTime pickup = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

        ReservationRequest request = new ReservationRequest(
                CarType.SEDAN,
                pickup,
                3
        );

        var firstReservation =
                reservationService.createReservation(request);

        var secondReservation =
                reservationService.createReservation(request);

        assertNotEquals(
                firstReservation.carId(),
                secondReservation.carId()
        );

        assertEquals(2, reservationRepository.count());
    }

    @Test
    void shouldAllowBackToBackReservationsOnSameCar() {

        carRepository.saveAndFlush(new Car(CarType.SEDAN));

        LocalDateTime firstPickup = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

        ReservationRequest firstRequest = new ReservationRequest(
                CarType.SEDAN,
                firstPickup,
                2
        );

        var firstReservation =
                reservationService.createReservation(firstRequest);

        LocalDateTime secondPickup =
                firstPickup.plusDays(2);

        ReservationRequest secondRequest = new ReservationRequest(
                CarType.SEDAN,
                secondPickup,
                2
        );

        var secondReservation =
                reservationService.createReservation(secondRequest);

        assertEquals(
                firstReservation.carId(),
                secondReservation.carId()
        );

        assertEquals(2, reservationRepository.count());
    }

    @Test
    void shouldRejectReservationWhenNoCarIsAvailable() {

        carRepository.saveAndFlush(new Car(CarType.SEDAN));

        LocalDateTime pickup = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

        ReservationRequest request = new ReservationRequest(
                CarType.SEDAN,
                pickup,
                3
        );

        reservationService.createReservation(request);

        assertThrows(
                CarNotAvailableException.class,
                () -> reservationService.createReservation(request)
        );

        assertEquals(1, reservationRepository.count());
    }

    @Test
    void shouldCancelReservationSuccessfully() {

        carRepository.saveAndFlush(new Car(CarType.SEDAN));

        LocalDateTime pickup = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

        ReservationRequest request = new ReservationRequest(
                CarType.SEDAN,
                pickup,
                3
        );

        var created =
                reservationService.createReservation(request);

        var cancelled =
                reservationService.cancelReservation(
                        created.reservationId()
                );

        assertEquals(
                ReservationStatus.CANCELLED,
                cancelled.status()
        );

        assertEquals(1, reservationRepository.count());
    }

    @Test
    void shouldAllowVehicleToBeReservedAfterCancellation() {

        carRepository.saveAndFlush(new Car(CarType.SEDAN));

        LocalDateTime pickup = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

        ReservationRequest request = new ReservationRequest(
                CarType.SEDAN,
                pickup,
                3
        );

        var first =
                reservationService.createReservation(request);

        reservationService.cancelReservation(first.reservationId());

        var second =
                reservationService.createReservation(request);

        assertEquals(first.carId(), second.carId());
        assertEquals(2, reservationRepository.count());
        assertEquals(
                ReservationStatus.CANCELLED,
                reservationRepository.findById(first.reservationId())
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void shouldAllowRepeatedCancellationWithoutChangingStatus() {

        carRepository.saveAndFlush(new Car(CarType.SEDAN));

        LocalDateTime pickup = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

        ReservationRequest request = new ReservationRequest(
                CarType.SEDAN,
                pickup,
                3
        );

        var created =
                reservationService.createReservation(request);

        reservationService.cancelReservation(created.reservationId());

        var cancelledAgain =
                reservationService.cancelReservation(
                        created.reservationId()
                );

        assertEquals(
                ReservationStatus.CANCELLED,
                cancelledAgain.status()
        );

        assertEquals(1, reservationRepository.count());
    }

}
