package com.example.carrental.entity;

import com.example.carrental.enums.CarType;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarType type;

    @Column(nullable = false)
    private boolean active = true;

    protected Car() {
    }

    public Car(CarType type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public CarType getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }
}