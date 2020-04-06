package com.leyou.common.exception;

import lombok.Getter;
import org.joda.time.DateTime;

@Getter
public class ExceptionResult {
    private int status;//响应状态码
    private String message;//响应信息
    private String timestamp;//响应时间搓

    public ExceptionResult(LyException e) {
        this.status = e.getStatus();
        this.message = e.getMessage();
        this.timestamp = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
    }
}
