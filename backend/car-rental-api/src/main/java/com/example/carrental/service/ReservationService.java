
package com.example.carrental.service;

import com.example.carrental.dto.ReservationRequest;
import com.example.carrental.dto.ReservationResponse;
import com.example.carrental.entity.Car;
import com.example.carrental.entity.Reservation;
import com.example.carrental.enums.ReservationStatus;
import com.example.carrental.exception.CarNotAvailableException;
import com.example.carrental.exception.ReservationNotFoundException;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final CarRepository carRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(
            CarRepository carRepository,
            ReservationRepository reservationRepository) {
        this.carRepository = carRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationResponse createReservation(
            ReservationRequest request) {

        LocalDateTime pickupDateTime = request.pickupDateTime();

        if (pickupDateTime == null ||
                !pickupDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Pickup date and time must be in the future"
            );
        }

        if (request.numberOfDays() == null ||
                request.numberOfDays() <= 0) {
            throw new IllegalArgumentException(
                    "Rental duration must be at least one day"
            );
        }

        LocalDateTime returnDateTime =
                pickupDateTime.plusDays(request.numberOfDays());

        // Lock vehicles of the requested type while allocating one.
        List<Car> cars =
                carRepository.findByTypeAndActiveTrueOrderByIdAsc(
                        request.carType()
                );

        for (Car car : cars) {

            long overlappingReservations =
                    reservationRepository.countOverlappingReservations(
                            car.getId(),
                            pickupDateTime,
                            returnDateTime,
                            ReservationStatus.CONFIRMED
                    );

            if (overlappingReservations == 0) {

                Reservation reservation = new Reservation(
                        car,
                        pickupDateTime,
                        returnDateTime
                );

                Reservation saved =
                        reservationRepository.save(reservation);

                return toResponse(saved);
            }
        }

        throw new CarNotAvailableException(
                "No " + request.carType() +
                " is available for the requested dates"
        );
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(UUID reservationId) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found: " + reservationId
                                )
                        );

        return toResponse(reservation);
    }


    @Transactional
    public ReservationResponse cancelReservation(UUID reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() ->
                        new ReservationNotFoundException(
                                "Reservation not found: " + reservationId
                        )
                );

        // Idempotent cancellation: repeated requests remain safe.
        if (reservation.getStatus() != ReservationStatus.CANCELLED) {
            reservation.cancel();
        }

        return toResponse(reservation);
    }

    private ReservationResponse toResponse(Reservation reservation) {

        return new ReservationResponse(
                reservation.getId(),
                reservation.getCar().getId(),
                reservation.getCar().getType(),
                reservation.getStartDateTime(),
                reservation.getEndDateTime(),
                reservation.getStatus()
        );
    }
}
