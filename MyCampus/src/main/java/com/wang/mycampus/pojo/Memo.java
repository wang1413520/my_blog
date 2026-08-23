package com.wang.mycampus.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Memo {
    private Long id;
    private Long userId;
    private String content;
    private Integer status;     // 0-未完成，1-已完成
    private Integer isPinned;   // 0-未置顶，1-已置顶
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
