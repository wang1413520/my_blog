package com.wang.mycampus.vo;

import lombok.Data;

@Data
public class ConvertHistoryVO {
    private Long id;
    private String sourceName;
    private String sourceType;
    private String targetType;
    private Integer status;
    private String resultUrl;
    private String createdAt;
}