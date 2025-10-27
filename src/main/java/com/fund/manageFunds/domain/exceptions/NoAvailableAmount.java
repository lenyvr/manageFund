package com.fund.manageFunds.domain.exceptions;

public class NoAvailableAmount extends RuntimeException {
    public NoAvailableAmount(String message) {
        super(message);
    }

    public NoAvailableAmount(String clientEmail, String fundName) {
        super(String.format("The user %s doesn't have enough amount to invest in fund %s'.", clientEmail, fundName));
    }
}
