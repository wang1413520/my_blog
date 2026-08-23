package com.wang.mycampus.service;

import com.wang.mycampus.vo.*;

import java.util.List;

/**
 * 后台管理首页 Service
 */
public interface AdminDashboardService {

    /**
     * 概览统计
     */
    AdminDashboardOverviewVO getOverview();

    /**
     * 用户注册趋势
     */
    List<TrendItemVO> getUserRegisterTrend(Integer days);

    /**
     * 发帖趋势
     */
    List<TrendItemVO> getPostPublishTrend(Integer days);

    /**
     * 资料上传趋势
     */
    List<TrendItemVO> getResourceUploadTrend(Integer days);

    /**
     * 文件类型分布
     */
    List<FileTypeDistributionVO> getResourceFileTypeDistribution();

    /**
     * 热门资料 Top N
     */
    List<HotResourceVO> getResourceDownloadTop(Integer limit);

    /**
     * 文件夹上传成功/失败统计
     */
    FolderUploadStatVO getFolderUploadStat();

    /**
     * 最新上传批次列表
     */
    List<LatestUploadBatchVO> getLatestUploadBatches(Integer limit);
}
