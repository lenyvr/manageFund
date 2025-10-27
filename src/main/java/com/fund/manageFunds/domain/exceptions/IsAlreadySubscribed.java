package com.fund.manageFunds.domain.exceptions;

public class IsAlreadySubscribed extends RuntimeException {

    public IsAlreadySubscribed(String message) {
        super(message);
    }

    public IsAlreadySubscribed(String clientEmail, String fundName) {
        super(String.format("The user %s is alrready subscribed to the fund '%s'.", clientEmail, fundName));
    }
}
