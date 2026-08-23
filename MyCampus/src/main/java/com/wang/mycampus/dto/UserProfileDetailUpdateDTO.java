package com.wang.mycampus.dto;

import lombok.Data;

@Data
public class UserProfileDetailUpdateDTO {
    private String realName;
    private String gender;       // male / female / unknown
    private String birthday;     // yyyy-MM-dd，用字符串接收，Service层转LocalDate
    private String phone;
    private String email;
    private String school;
    private String college;
    private String major;
    private String grade;
    private String location;
    private String bio;
}
