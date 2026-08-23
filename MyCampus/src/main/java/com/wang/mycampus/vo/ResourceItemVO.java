package com.wang.mycampus.vo;

import lombok.Data;

@Data
public class ResourceItemVO {
    private Long id;
    private String title;
    private String description;
    private String fileType;
    private Long fileSize;
    private Integer downloadCount;
    private String uploaderName;
    private String uploadType;
    private String folderName;
    private String relativePath;
    private String batchNo;
    private String createTime;
}

