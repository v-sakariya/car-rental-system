
CREATE TABLE cars (
    id UUID PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_car_type
        CHECK (type IN ('SEDAN', 'SUV', 'VAN'))
);

CREATE TABLE reservations (
    id UUID PRIMARY KEY,

    car_id UUID NOT NULL,

    start_date_time TIMESTAMP NOT NULL,
    end_date_time TIMESTAMP NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservation_car
        FOREIGN KEY (car_id)
        REFERENCES cars(id),

    CONSTRAINT chk_reservation_status
        CHECK (status IN ('CONFIRMED', 'CANCELLED')),

    CONSTRAINT chk_reservation_dates
        CHECK (end_date_time > start_date_time)
);

CREATE INDEX idx_cars_type_active
    ON cars(type, active);

CREATE INDEX idx_reservations_car_dates
    ON reservations(car_id, start_date_time, end_date_time);
