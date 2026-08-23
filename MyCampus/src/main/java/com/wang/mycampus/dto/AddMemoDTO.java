package com.wang.mycampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddMemoDTO {
    @NotBlank(message = "备忘录内容不能为空")
    @Size(max = 120, message = "备忘录内容不能超过120个字符")
    private String content;
}
