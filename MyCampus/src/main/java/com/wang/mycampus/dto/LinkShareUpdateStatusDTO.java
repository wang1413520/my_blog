package com.wang.mycampus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LinkShareUpdateStatusDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
