package com.leyou.common.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装当前用户的信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private Long id;
    private String username;
    private String role;
}
