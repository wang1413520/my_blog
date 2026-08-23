package com.wang.mycampus.controller;

import com.wang.mycampus.annotation.RequireAdmin;
import com.wang.mycampus.exception.BaseException;
import com.wang.mycampus.service.AdminDashboardService;
import com.wang.mycampus.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理首页 Controller
 * 所有接口仅管理员可访问
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    /*
     * 6.1 概览统计
     * */
    @RequireAdmin
    @GetMapping("/overview")
    public Result<AdminDashboardOverviewVO> getOverview() {
        log.info("后台首页 - 概览统计");
        AdminDashboardOverviewVO vo = adminDashboardService.getOverview();
        return Result.success(vo);
    }

    /*
     * 6.2 用户注册趋势
     * */
    @RequireAdmin
    @GetMapping("/user-register-trend")
    public Result<List<TrendItemVO>> getUserRegisterTrend(
            @RequestParam(defaultValue = "7") Integer days) {
        log.info("后台首页 - 用户注册趋势 days={}", days);
        validateDays(days);
        List<TrendItemVO> list = adminDashboardService.getUserRegisterTrend(days);
        return Result.success(list);
    }

    /*
     * 6.3 发帖趋势
     * */
    @RequireAdmin
    @GetMapping("/post-publish-trend")
    public Result<List<TrendItemVO>> getPostPublishTrend(
            @RequestParam(defaultValue = "7") Integer days) {
        log.info("后台首页 - 发帖趋势 days={}", days);
        validateDays(days);
        List<TrendItemVO> list = adminDashboardService.getPostPublishTrend(days);
        return Result.success(list);
    }

    /*
     * 6.4 资料上传趋势
     * */
    @RequireAdmin
    @GetMapping("/resource-upload-trend")
    public Result<List<TrendItemVO>> getResourceUploadTrend(
            @RequestParam(defaultValue = "7") Integer days) {
        log.info("后台首页 - 资料上传趋势 days={}", days);
        validateDays(days);
        List<TrendItemVO> list = adminDashboardService.getResourceUploadTrend(days);
        return Result.success(list);
    }

    /*
     * 6.5 文件类型分布
     * */
    @RequireAdmin
    @GetMapping("/resource-file-type-distribution")
    public Result<List<FileTypeDistributionVO>> getResourceFileTypeDistribution() {
        log.info("后台首页 - 文件类型分布");
        List<FileTypeDistributionVO> list = adminDashboardService.getResourceFileTypeDistribution();
        return Result.success(list);
    }

    /*
     * 6.6 热门资料 Top N
     * */
    @RequireAdmin
    @GetMapping("/resource-download-top")
    public Result<List<HotResourceVO>> getResourceDownloadTop(
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("后台首页 - 热门资料Top limit={}", limit);
        if (limit == null || limit < 1 || limit > 20) {
            return Result.error(400, "limit 参数不合法，取值范围 1-20");
        }
        List<HotResourceVO> list = adminDashboardService.getResourceDownloadTop(limit);
        return Result.success(list);
    }

    /*
     * 6.7 文件夹上传成功/失败统计
     * */
    @RequireAdmin
    @GetMapping("/folder-upload-stat")
    public Result<FolderUploadStatVO> getFolderUploadStat() {
        log.info("后台首页 - 文件夹上传统计");
        FolderUploadStatVO vo = adminDashboardService.getFolderUploadStat();
        return Result.success(vo);
    }

    /*
     * 6.8 最新上传批次列表
     * */
    @RequireAdmin
    @GetMapping("/latest-upload-batches")
    public Result<List<LatestUploadBatchVO>> getLatestUploadBatches(
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("后台首页 - 最新上传批次 limit={}", limit);
        if (limit == null || limit < 1 || limit > 20) {
            return Result.error(400, "limit 参数不合法，取值范围 1-20");
        }
        List<LatestUploadBatchVO> list = adminDashboardService.getLatestUploadBatches(limit);
        return Result.success(list);
    }

    // ========== 参数校验 ==========

    private void validateDays(Integer days) {
        if (days == null || (days != 7 && days != 30)) {
            throw new BaseException(400, "days 参数仅支持 7 或 30");
        }
    }
}
