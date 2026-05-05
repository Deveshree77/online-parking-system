package com.smartpark.exception;

public class SlotLockExpiredException extends RuntimeException {
    public SlotLockExpiredException(String message) {
        super(message);
    }
}
