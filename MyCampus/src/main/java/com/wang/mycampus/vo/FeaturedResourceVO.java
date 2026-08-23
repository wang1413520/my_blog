package com.wang.mycampus.vo;

import lombok.Data;

@Data
public class FeaturedResourceVO {
    private Long id;
    private Long resourceId;
    private String title;
    private String description;
    private String coverUrl;
    private String fileType;
    private Long fileSize;
    private Integer downloadCount;
    private String uploaderName;
    private String createTime;
    private Integer sort;
    private Integer status;
}