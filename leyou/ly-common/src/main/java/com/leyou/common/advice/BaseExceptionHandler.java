package com.leyou.common.advice;

import com.leyou.common.exception.ExceptionResult;
import com.leyou.common.exception.LyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler(LyException.class)//指定只处理 LyException
    private ResponseEntity<ExceptionResult> exceptionHandler(LyException e){

        //e中已经包含了我们定义的异常状态码
        return ResponseEntity
                .status(e.getStatus())
                .body(new ExceptionResult(e));
    }
}
