package com.wang.mycampus.dto;

import lombok.Data;

@Data
public class  PublishPostDTO {
    private String title;       // 标题，<= 100字
    private String content;     // 内容，<= 2000字
    private Integer type;       // 0-吐槽，1-建议
    private Integer isAnonymous; // 0-实名，1-匿名（可选，默认0）
}