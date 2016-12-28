package com.kiosktm;

public class AuthenticationException extends Exception {

    private static final String MESSAGE = "Invalid client_id or client_secret, or other auth related error";

    public AuthenticationException() {
        super(AuthenticationException.MESSAGE);
    }

}
