package com.wang.mycampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoticeDTO {

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 100, message = "标题不能超过100字符")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    @Size(max = 2000, message = "内容不能超过2000字符")
    private String content;

    private Integer status;   // 1启用 0停用，缺省时默认1
}
