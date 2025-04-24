package com.example.weather.customException;

public class RateLimitExceededException extends RuntimeException{

    private int statusCode;
    private String message;
    private String retryAfter; // Optional: to indicate when the user can retry


    // Constructor
    public RateLimitExceededException(int statusCode, String message, String retryAfter) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
        this.retryAfter = retryAfter;
    }

    // Getters
    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String getRetryAfter() {
        return retryAfter;
    }

}
