package com.wang.mycampus.dto;

import lombok.Data;

@Data
public class FeaturedResourceQueryDTO {

    private Integer page;

    private Integer size;

    private String keyword;

    private Integer status;
}