package com.wang.mycampus.pojo;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class Post {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer type;        // 0-吐槽，1-建议
    private Integer isAnonymous; // 0-实名，1-匿名
    private Integer likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}