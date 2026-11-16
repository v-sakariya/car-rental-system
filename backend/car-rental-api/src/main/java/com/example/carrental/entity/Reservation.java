package com.example.carrental.entity;

import com.example.carrental.enums.ReservationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    protected Reservation() {
    }


    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public Reservation(
            Car car,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime) {
        this.car = car;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = ReservationStatus.CONFIRMED;
    }

    public UUID getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}