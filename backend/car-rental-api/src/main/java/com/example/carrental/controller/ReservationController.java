
package com.example.carrental.controller;

import com.example.carrental.dto.ReservationRequest;
import com.example.carrental.dto.ReservationResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import com.example.carrental.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(
            @Valid @RequestBody ReservationRequest request) {

        return reservationService.createReservation(request);
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(
            @PathVariable UUID id) {

        return reservationService.getReservation(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable UUID id) {

        ReservationResponse response =
                reservationService.cancelReservation(id);

        return ResponseEntity.ok(response);
    }

}
