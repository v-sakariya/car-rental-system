
package com.example.carrental.repository;

import com.example.carrental.entity.Reservation;
import com.example.carrental.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ReservationRepository
        extends JpaRepository<Reservation, UUID> {

    @Query("""
        SELECT COUNT(r)
        FROM Reservation r
        WHERE r.car.id = :carId
          AND r.status = :status
          AND r.startDateTime < :requestedEnd
          AND r.endDateTime > :requestedStart
        """)
    long countOverlappingReservations(
            @Param("carId") UUID carId,
            @Param("requestedStart") LocalDateTime requestedStart,
            @Param("requestedEnd") LocalDateTime requestedEnd,
            @Param("status") ReservationStatus status
    );
}
