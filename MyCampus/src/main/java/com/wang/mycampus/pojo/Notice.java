package com.wang.mycampus.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notice {
    private Long id;
    private String title;
    private String content;
    private Integer status;   // 1启用 0停用
    private LocalDateTime createTime;
    private LocalDateTime updateTime; // 版本号，前端据此判断公告是否有更新
}
