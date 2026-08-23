package com.wang.mycampus.vo;

import lombok.Data;

/**
 * 后台首页概览统计 VO
 */
@Data
public class AdminDashboardOverviewVO {
    private Long userCount;
    private Long postCount;
    private Long resourceCount;
    private Long totalDownloadCount;
}
