package com.wang.mycampus.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;  // 用户名，4-20位
    private String password;  // 密码，6-20位
    private String nickname;  // 昵称（可选）
}
