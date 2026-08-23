package com.wang.mycampus.vo;

import lombok.Data;

/**
 * 趋势数据项 VO（用户注册趋势 / 发帖趋势 / 资料上传趋势 共用）
 */
@Data
public class TrendItemVO {
    private String date;
    private Long count;
}
