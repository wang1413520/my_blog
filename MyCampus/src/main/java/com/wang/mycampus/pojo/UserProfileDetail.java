package com.wang.mycampus.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserProfileDetail {
    private Long id;
    private Long userId;
    private String realName;
    private String gender;       // male / female / unknown
    private LocalDate birthday;
    private String phone;
    private String email;
    private String school;
    private String college;
    private String major;
    private String grade;
    private String location;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
