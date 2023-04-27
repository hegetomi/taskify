package com.hegetomi.taskify.exception;

public class UserExistsException extends RuntimeException {
    public UserExistsException() {
        super("Username is taken");
    }
}
