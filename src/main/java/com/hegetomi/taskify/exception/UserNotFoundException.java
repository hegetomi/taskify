package com.hegetomi.taskify.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException() {
        super("User was not found");
    }
}
