package com.wang.mycampus.dto;

import lombok.Data;

@Data
public class LinkShareQueryDTO {
    private Integer page;
    private Integer size;
    private String keyword;
    private String sourceName;
}
