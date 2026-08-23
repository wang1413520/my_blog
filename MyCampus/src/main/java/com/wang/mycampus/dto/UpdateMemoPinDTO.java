package com.wang.mycampus.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMemoPinDTO {
    @NotNull(message = "置顶状态不能为空")
    @Min(value = 0, message = "置顶状态值不合法")
    @Max(value = 1, message = "置顶状态值不合法")
    private Integer isPinned;
}
