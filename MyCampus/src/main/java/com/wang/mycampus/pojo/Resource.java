package com.wang.mycampus.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Resource {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Integer downloadCount;
    private String uploadType;
    private String batchNo;
    private String folderName;
    private String relativePath;
    private LocalDateTime createTime;
}