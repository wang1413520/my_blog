package com.wang.mycampus.dto;

import lombok.Data;

@Data
public class ResourceMyListQueryDTO {
    private Integer page;
    private Integer size;
    private String keyword;
    private String fileType;    // pdf / doc / docx
    private String uploadType;  // single / folder
}
