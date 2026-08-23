package com.wang.mycampus.handler;


import com.wang.mycampus.exception.BaseException;
import com.wang.mycampus.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    /*
    * 捕获业务异常
    *
    * */
    @ExceptionHandler
    public Result globalExceptionHandler(BaseException ex){
        log.info("异常信息为: {}",ex.getMessage());
        return Result.error(ex.getCode(),ex.getMessage());
    }

    /*
     * 捕获参数校验异常（@Valid）
     * 返回第一条校验失败信息给前端
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("参数校验失败");
        log.info("参数校验异常: {}", message);
        return Result.error(400, message);
    }

}
