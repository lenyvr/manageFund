package com.fund.manageFunds.domain.exceptions;

public class NoFundFound extends RuntimeException{

    public NoFundFound(String message) {
        super(message);
    }

    public NoFundFound(String clientEmail, String fundName) {
        super(String.format("Fund '%s not found, the user %s can't be subscribed'.", fundName, clientEmail));
    }
}
