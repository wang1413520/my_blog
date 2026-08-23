package com.wang.mycampus.vo;

import lombok.Data;

@Data
public class MemoVO {
    private Long id;
    private String content;
    private Integer status;     // 0-未完成，1-已完成
    private Integer isPinned;   // 0-未置顶，1-已置顶
    private Long createTime;    // 13位毫秒时间戳
    private Long updateTime;    // 13位毫秒时间戳
}
