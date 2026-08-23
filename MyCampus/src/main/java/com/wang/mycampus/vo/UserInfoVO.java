package com.wang.mycampus.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer role;
    private String createTime;
}
