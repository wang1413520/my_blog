package com.wang.mycampus.vo;

import lombok.Data;

@Data
public class ResourceUploadVO {
    private Long id;
    private String title;
    private String fileType;
    private Long fileSize;
    private String createTime;
}
