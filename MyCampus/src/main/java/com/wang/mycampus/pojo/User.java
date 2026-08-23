package com.wang.mycampus.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private Integer role;        // 0-普通用户，1-管理员
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}