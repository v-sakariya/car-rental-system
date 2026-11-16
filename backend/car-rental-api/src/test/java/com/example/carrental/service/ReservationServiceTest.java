
package com.example.carrental.service;

import com.example.carrental.dto.ReservationRequest;
import com.example.carrental.dto.ReservationResponse;
import com.example.carrental.entity.Car;
import com.example.carrental.entity.Reservation;
import com.example.carrental.enums.CarType;
import com.example.carrental.enums.ReservationStatus;
import com.example.carrental.exception.CarNotAvailableException;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.ReservationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
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
class ReservationServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private LocalDateTime pickup;
    private LocalDateTime returnDate;

    @BeforeEach
    void setUp() {
        pickup = LocalDateTime.now().plusDays(10);
        returnDate = pickup.plusDays(3);
    }

    @Test
    void shouldCreateReservationWhenCarIsAvailable() {

        Car car = mock(Car.class);

        UUID carId = UUID.randomUUID();
        when(car.getId()).thenReturn(carId);
        when(car.getType()).thenReturn(CarType.SEDAN);

        when(carRepository
                .findByTypeAndActiveTrueOrderByIdAsc(CarType.SEDAN))
                .thenReturn(List.of(car));

        when(reservationRepository.countOverlappingReservations(
                eq(carId),
                eq(pickup),
                eq(returnDate),
                eq(ReservationStatus.CONFIRMED)))
                .thenReturn(0L);

        Reservation savedReservation = mock(Reservation.class);

        UUID reservationId = UUID.randomUUID();

        when(savedReservation.getId()).thenReturn(reservationId);
        when(savedReservation.getCar()).thenReturn(car);
        when(savedReservation.getStartDateTime()).thenReturn(pickup);
        when(savedReservation.getEndDateTime()).thenReturn(returnDate);
        when(savedReservation.getStatus())
                .thenReturn(ReservationStatus.CONFIRMED);

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(savedReservation);

        ReservationRequest request = new ReservationRequest(
                CarType.SEDAN,
                pickup,
                3
        );

        ReservationResponse response =
                reservationService.createReservation(request);

        assertNotNull(response);
        assertEquals(reservationId, response.reservationId());
        assertEquals(carId, response.carId());
        assertEquals(CarType.SEDAN, response.carType());
        assertEquals(pickup, response.pickupDateTime());
        assertEquals(returnDate, response.returnDateTime());
        assertEquals(
                ReservationStatus.CONFIRMED,
                response.status()
        );

        verify(reservationRepository, times(1))
                .save(any(Reservation.class));
    }

    @Test
    void shouldUseDifferentCarWhenFirstCarIsAlreadyReserved() {

        Car firstCar = mock(Car.class);
        Car secondCar = mock(Car.class);

        UUID firstCarId = UUID.randomUUID();
        UUID secondCarId = UUID.randomUUID();

        when(firstCar.getId()).thenReturn(firstCarId);
        when(secondCar.getId()).thenReturn(secondCarId);

        when(carRepository
                .findByTypeAndActiveTrueOrderByIdAsc(CarType.SEDAN))
                .thenReturn(List.of(firstCar, secondCar));

        when(reservationRepository.countOverlappingReservations(
                eq(firstCarId), eq(pickup), eq(returnDate),
                eq(ReservationStatus.CONFIRMED)))
                .thenReturn(1L);

        when(reservationRepository.countOverlappingReservations(
                eq(secondCarId), eq(pickup), eq(returnDate),
                eq(ReservationStatus.CONFIRMED)))
                .thenReturn(0L);

        Reservation savedReservation = mock(Reservation.class);

        when(savedReservation.getId()).thenReturn(UUID.randomUUID());
        when(savedReservation.getCar()).thenReturn(secondCar);
        when(savedReservation.getStartDateTime()).thenReturn(pickup);
        when(savedReservation.getEndDateTime()).thenReturn(returnDate);
        when(savedReservation.getStatus())
                .thenReturn(ReservationStatus.CONFIRMED);

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(savedReservation);

        ReservationRequest request = new ReservationRequest(
                CarType.SEDAN, pickup, 3
        );

        ReservationResponse response =
                reservationService.createReservation(request);

        assertEquals(secondCarId, response.carId());

        ArgumentCaptor<Reservation> captor =
                ArgumentCaptor.forClass(Reservation.class);

        verify(reservationRepository).save(captor.capture());

        assertEquals(
                secondCar,
                captor.getValue().getCar()
        );
    }

    @Test
    void shouldThrowExceptionWhenNoCarsAreAvailable() {

        Car car = mock(Car.class);

        UUID carId = UUID.randomUUID();
        when(car.getId()).thenReturn(carId);

        when(carRepository
                .findByTypeAndActiveTrueOrderByIdAsc(CarType.SEDAN))
                .thenReturn(List.of(car));

        when(reservationRepository.countOverlappingReservations(
                eq(carId), eq(pickup), eq(returnDate),
                eq(ReservationStatus.CONFIRMED)))
                .thenReturn(1L);

        ReservationRequest request = new ReservationRequest(
                CarType.SEDAN, pickup, 3
        );

        assertThrows(
                CarNotAvailableException.class,
                () -> reservationService.createReservation(request)
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void shouldRejectPickupDateInThePast() {

        ReservationRequest request = new ReservationRequest(
                CarType.SEDAN,
                LocalDateTime.now().minusDays(1),
                3
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(request)
        );

        verifyNoInteractions(carRepository);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldRejectZeroRentalDays() {

        ReservationRequest request = new ReservationRequest(
                CarType.SEDAN, pickup, 0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(request)
        );

        verifyNoInteractions(carRepository);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldRejectNegativeRentalDays() {

        ReservationRequest request = new ReservationRequest(
                CarType.SEDAN, pickup, -2
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(request)
        );

        verifyNoInteractions(carRepository);
        verifyNoInteractions(reservationRepository);
    }
}
