package com.wang.mycampus.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {
    private List<T> records;
    private Integer total;
    private Integer page;
    private Integer size;
}
