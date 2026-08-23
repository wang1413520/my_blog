package com.wang.mycampus.exception;

public class LoginException extends BaseException {

    public LoginException(String message) {
        super(message);
    }


    public LoginException(int code,String message){
        super(code,message);
    }

}
