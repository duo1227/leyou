package com.leyou.common.auth.entity;

import lombok.Data;

import java.util.Date;

/**
 * 封装JWT中的载荷部分
 * @param <T>
 */
@Data
public class Payload<T> {
    private String id;
    private T userInfo;
    private Date expiration;
}
