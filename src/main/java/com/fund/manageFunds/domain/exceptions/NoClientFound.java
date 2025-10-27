package com.fund.manageFunds.domain.exceptions;

public class NoClientFound extends RuntimeException{
    public NoClientFound(String message) {
        super(message);
    }

    public NoClientFound(String clientEmail, String fundName) {
        super(String.format("Fund '%s not found, the user %s can't be subscribed'.", fundName, clientEmail));
    }
}
