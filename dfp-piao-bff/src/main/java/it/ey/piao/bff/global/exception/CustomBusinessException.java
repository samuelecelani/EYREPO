package it.ey.piao.bff.global.exception;


public class CustomBusinessException extends RuntimeException {
    public CustomBusinessException(String message) {
        super(message);
    }
}

