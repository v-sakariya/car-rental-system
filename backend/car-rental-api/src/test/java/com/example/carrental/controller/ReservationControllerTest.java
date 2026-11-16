
package com.example.carrental.controller;

import com.example.carrental.dto.ReservationResponse;
import com.example.carrental.enums.CarType;
import com.example.carrental.enums.ReservationStatus;
import com.example.carrental.service.ReservationService;
import com.example.carrental.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private MockMvc mockMvc;

    private UUID reservationId;
    private UUID carId;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(reservationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        reservationId = UUID.randomUUID();
        carId = UUID.randomUUID();
    }

    @Test
    void shouldCreateReservationSuccessfully() throws Exception {

        LocalDateTime pickup =
                LocalDateTime.now().plusDays(10).withNano(0);

        ReservationResponse response = new ReservationResponse(
                reservationId,
                carId,
                CarType.SEDAN,
                pickup,
                pickup.plusDays(3),
                ReservationStatus.CONFIRMED
        );

        when(reservationService.createReservation(any()))
                .thenReturn(response);

        String requestBody = """
                {
                    "carType": "SEDAN",
                    "pickupDateTime": "%s",
                    "numberOfDays": 3
                }
                """.formatted(pickup);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId")
                        .value(reservationId.toString()))
                .andExpect(jsonPath("$.carId")
                        .value(carId.toString()))
                .andExpect(jsonPath("$.carType").value("SEDAN"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService, times(1))
                .createReservation(any());
    }

    @Test
    void shouldRetrieveReservationSuccessfully() throws Exception {

        LocalDateTime pickup =
                LocalDateTime.now().plusDays(10).withNano(0);

        ReservationResponse response = new ReservationResponse(
                reservationId,
                carId,
                CarType.SEDAN,
                pickup,
                pickup.plusDays(3),
                ReservationStatus.CONFIRMED
        );

        when(reservationService.getReservation(reservationId))
                .thenReturn(response);

        mockMvc.perform(get("/api/reservations/{id}", reservationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId")
                        .value(reservationId.toString()))
                .andExpect(jsonPath("$.carType").value("SEDAN"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService, times(1))
                .getReservation(reservationId);
    }
}
