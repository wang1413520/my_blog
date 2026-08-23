package com.wang.mycampus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeaturedResourceAddDTO {

    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    private String featuredTitle;

    private String featuredDesc;

    private String coverUrl;

    private Integer sort;

    private Integer status;
}