package com.wang.mycampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class LinkShareAddDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "链接不能为空")
    private String linkUrl;

    private String sourceName;

    private List<String> tags;

    private Integer sort;

    private Integer status;
}
