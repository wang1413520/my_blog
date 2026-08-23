package com.wang.mycampus.dto;

import lombok.Data;

@Data
public class FeaturedResourceUpdateDTO {

    private String featuredTitle;

    private String featuredDesc;

    private String coverUrl;

    private Integer sort;

    private Integer status;
}