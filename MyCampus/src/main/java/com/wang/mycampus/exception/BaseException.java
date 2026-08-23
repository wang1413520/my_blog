package com.wang.mycampus.exception;

public class BaseException extends RuntimeException{
    public BaseException(String message) {
        super(message);
    }


    private int code;

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
