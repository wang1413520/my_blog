package com.wang.mycampus.exception;

public class RegisterException extends BaseException {

    public RegisterException(String message) {
        super(message);
    }

    public RegisterException(int code, String message) {
        super(code, message);
    }
}
