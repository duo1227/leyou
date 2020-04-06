package com.leyou.common.exception;


import com.leyou.common.enums.ExceptionEnum;
import lombok.Getter;

@Getter
public class LyException extends RuntimeException {

    private Integer status;

    public LyException (Integer status,String message){
        super(message);
        this.status = status;
    }

    public LyException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMessage());
        this.status = exceptionEnum.getStatus();
    }
}
