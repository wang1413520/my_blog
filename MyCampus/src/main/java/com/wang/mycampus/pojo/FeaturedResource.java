package com.wang.mycampus.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeaturedResource {
    private Long id;
    private Long resourceId;
    private String featuredTitle;
    private String featuredDesc;
    private String coverUrl;
    private Integer sort;
    private Integer status;         // 0-停用 1-启用
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}