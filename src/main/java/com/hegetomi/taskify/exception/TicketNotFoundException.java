package com.hegetomi.taskify.exception;

public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String message) {
        super(message);
    }
}
