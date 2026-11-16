
package com.example.carrental.service;

import com.example.carrental.dto.AvailabilityResponse;
import com.example.carrental.dto.ReservationRequest;
import com.example.carrental.dto.ReservationResponse;
import com.example.carrental.enums.CarType;
import com.example.carrental.exception.CarNotAvailableException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReservationConcurrencyIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private AvailabilityService availabilityService;

    @Test
    void shouldPreventDoubleBookingDuringConcurrentReservations()
            throws Exception {

        CarType carType = CarType.SEDAN;

        LocalDateTime pickupDateTime = LocalDateTime.now()
                .plusDays(730)
                .withNano(0);

        int rentalDays = 2;

        AvailabilityResponse availability =
                availabilityService.checkAvailability(
                        carType,
                        pickupDateTime,
                        rentalDays
                );

        int availableCars = availability.availableVehicles();

        assertTrue(
                availableCars > 0,
                "At least one vehicle must be available for this test"
        );

        int concurrentRequests = availableCars + 1;

        ExecutorService executor =
                Executors.newFixedThreadPool(concurrentRequests);

        CountDownLatch readyLatch =
                new CountDownLatch(concurrentRequests);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<UUID> reservationIds =
                new CopyOnWriteArrayList<>();

        List<UUID> allocatedCarIds =
                new CopyOnWriteArrayList<>();

        List<Future<Boolean>> futures = new ArrayList<>();

        try {

            ReservationRequest request = new ReservationRequest(
                    carType,
                    pickupDateTime,
                    rentalDays
            );

            for (int i = 0; i < concurrentRequests; i++) {

                Future<Boolean> future = executor.submit(() -> {

                    readyLatch.countDown();

                    if (!startLatch.await(15, TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                                "Concurrent test start timed out"
                        );
                    }

                    try {

                        ReservationResponse response =
                                reservationService.createReservation(request);

                        reservationIds.add(response.reservationId());
                        allocatedCarIds.add(response.carId());

                        return true;

                    } catch (CarNotAvailableException exception) {
                        return false;
                    }
                });

                futures.add(future);
            }

            assertTrue(
                    readyLatch.await(15, TimeUnit.SECONDS),
                    "Not all threads became ready"
            );

            // Release all threads at approximately the same time.
            startLatch.countDown();

            int successfulReservations = 0;
            int rejectedReservations = 0;

            for (Future<Boolean> future : futures) {

                boolean success = future.get(30, TimeUnit.SECONDS);

                if (success) {
                    successfulReservations++;
                } else {
                    rejectedReservations++;
                }
            }

            assertEquals(
                    availableCars,
                    successfulReservations,
                    "Every available vehicle should be allocated exactly once"
            );

            assertEquals(
                    1,
                    rejectedReservations,
                    "One request should be rejected because inventory is exhausted"
            );

            assertEquals(
                    successfulReservations,
                    Set.copyOf(allocatedCarIds).size(),
                    "No vehicle should be allocated to multiple reservations"
            );

            assertEquals(
                    successfulReservations,
                    Set.copyOf(reservationIds).size(),
                    "Every successful request should have a unique reservation ID"
            );

        } finally {

            // Release waiting threads if an earlier assertion fails.
            startLatch.countDown();

            // Cancel test reservations so inventory remains unchanged.
            for (UUID reservationId : reservationIds) {
                reservationService.cancelReservation(reservationId);
            }

            executor.shutdownNow();
        }
    }
}
