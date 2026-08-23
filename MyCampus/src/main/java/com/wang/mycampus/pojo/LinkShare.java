package com.wang.mycampus.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LinkShare {
    private Long id;
    private String title;
    private String description;
    private String linkUrl;
    private String sourceName;
    private String tags;            // 数据库 tags_json，Service 层负责 List<String> 与 JSON 互转
    private Integer sort;
    private Integer status;         // 0-禁用 1-启用
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
