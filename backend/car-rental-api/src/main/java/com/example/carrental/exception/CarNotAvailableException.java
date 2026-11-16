package com.example.carrental.exception;

public class CarNotAvailableException extends RuntimeException {

    public CarNotAvailableException(String message) {
        super(message);
    }
}