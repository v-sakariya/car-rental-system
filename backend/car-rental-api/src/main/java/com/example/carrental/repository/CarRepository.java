
package com.example.carrental.repository;

import com.example.carrental.entity.Car;
import com.example.carrental.enums.CarType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {

    List<Car> findByTypeAndActiveTrue(CarType type);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Car> findByTypeAndActiveTrueOrderByIdAsc(CarType type);
}
