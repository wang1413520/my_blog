package com.wang.mycampus.exception;

public class PasswordException extends BaseException{

    public PasswordException(String message) {
        super(message);
    }

    public PasswordException(int code ,String message){super(code, message);}

}
