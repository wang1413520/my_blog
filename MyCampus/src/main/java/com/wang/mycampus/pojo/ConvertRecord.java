package com.wang.mycampus.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConvertRecord {
    private Long id;
    private Long userId;
    private String sourceName;
    private String sourceType;
    private String targetType;
    private Integer status;         // 0-转换中 1-成功 2-失败
    private String sourceUrl;
    private String resultUrl;
    private Long fileSize;
    private String errorMsg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}