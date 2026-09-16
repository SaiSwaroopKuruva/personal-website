package com.example.helloworld.exception;

public class MobileAlreadyExistsException extends RuntimeException {
    public MobileAlreadyExistsException(String message) {
        super(message);
    }
}
