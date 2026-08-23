package com.wang.mycampus.vo;

import lombok.Data;

/**
 * 热门资料 VO
 */
@Data
public class HotResourceVO {
    private Long id;
    private String title;
    private Long downloadCount;
    private String fileType;
}
