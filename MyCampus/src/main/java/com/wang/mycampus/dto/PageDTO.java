package com.wang.mycampus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PageDTO {
    private Integer type;
    private Integer page;
    private Integer size;
}
