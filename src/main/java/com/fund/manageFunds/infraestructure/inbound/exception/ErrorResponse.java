package com.fund.manageFunds.infraestructure.inbound.exception;

public record ErrorResponse(
        int status,
        String error,
        String message
) {}
