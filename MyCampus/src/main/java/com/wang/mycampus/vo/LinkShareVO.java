package com.wang.mycampus.vo;

import lombok.Data;

@Data
public class LinkShareVO {
    private Long id;
    private String title;
    private String description;
    private String linkUrl;
    private String sourceName;
    private String tags;            // 数据库 tags_json 原样返回，前端自行 JSON.parse
    private Integer sort;
    private Integer status;
    private String createdByName;
    private String createTime;
    private String updateTime;
}
