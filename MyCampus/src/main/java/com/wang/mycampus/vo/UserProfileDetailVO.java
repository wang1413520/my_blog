package com.wang.mycampus.vo;

import lombok.Data;

@Data
public class UserProfileDetailVO {
    private Long userId;
    private String realName;
    private String gender;
    private String birthday;     // yyyy-MM-dd
    private String phone;
    private String email;
    private String school;
    private String college;
    private String major;
    private String grade;
    private String location;
    private String bio;
    private String createTime;   // yyyy-MM-dd HH:mm:ss
    private String updateTime;   // yyyy-MM-dd HH:mm:ss
}
