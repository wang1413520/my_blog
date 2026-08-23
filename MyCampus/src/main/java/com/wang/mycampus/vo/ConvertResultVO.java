package com.wang.mycampus.vo;

import lombok.Data;

@Data
public class ConvertResultVO {
    private Long recordId;
    private String sourceName;
    private String sourceType;
    private String targetType;
    private Integer status;
    private String resultUrl;
    private Long fileSize;
}