package com.wang.mycampus.service.Impl;

import com.wang.mycampus.mapper.PostMapper;
import com.wang.mycampus.mapper.ResourceMapper;
import com.wang.mycampus.mapper.ResourceUploadBatchMapper;
import com.wang.mycampus.mapper.UserMapper;
import com.wang.mycampus.service.AdminDashboardService;
import com.wang.mycampus.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private ResourceUploadBatchMapper batchMapper;

    // ========== 1. 概览统计 ==========

    @Override
    public AdminDashboardOverviewVO getOverview() {
        AdminDashboardOverviewVO vo = new AdminDashboardOverviewVO();
        vo.setUserCount(userMapper.selectUserCount());
        vo.setPostCount((long) postMapper.selectSumNumber());
        vo.setResourceCount((long) resourceMapper.selectNumber());
        vo.setTotalDownloadCount(resourceMapper.selectTotalDownloadCount());
        return vo;
    }

    // ========== 2. 用户注册趋势 ==========

    @Override
    public List<TrendItemVO> getUserRegisterTrend(Integer days) {
        List<TrendItemVO> dbData = userMapper.selectUserRegisterTrend(days);
        return fillZeroDates(dbData, days);
    }

    // ========== 3. 发帖趋势 ==========

    @Override
    public List<TrendItemVO> getPostPublishTrend(Integer days) {
        List<TrendItemVO> dbData = postMapper.selectPostPublishTrend(days);
        return fillZeroDates(dbData, days);
    }

    // ========== 4. 资料上传趋势 ==========

    @Override
    public List<TrendItemVO> getResourceUploadTrend(Integer days) {
        List<TrendItemVO> dbData = resourceMapper.selectResourceUploadTrend(days);
        return fillZeroDates(dbData, days);
    }

    // ========== 5. 文件类型分布 ==========

    @Override
    public List<FileTypeDistributionVO> getResourceFileTypeDistribution() {
        List<FileTypeDistributionVO> list = resourceMapper.selectFileTypeDistribution();
        return list != null ? list : Collections.emptyList();
    }

    // ========== 6. 热门资料 Top N ==========

    @Override
    public List<HotResourceVO> getResourceDownloadTop(Integer limit) {
        List<HotResourceVO> list = resourceMapper.selectDownloadTop(limit);
        return list != null ? list : Collections.emptyList();
    }

    // ========== 7. 文件夹上传成功/失败统计 ==========

    @Override
    public FolderUploadStatVO getFolderUploadStat() {
        FolderUploadStatVO vo = batchMapper.selectBatchStats();
        if (vo == null) {
            vo = new FolderUploadStatVO();
            vo.setTotalBatchCount(0L);
            vo.setSuccessBatchCount(0L);
            vo.setPartialSuccessBatchCount(0L);
            vo.setFailedBatchCount(0L);
        }
        return vo;
    }

    // ========== 8. 最新上传批次列表 ==========

    @Override
    public List<LatestUploadBatchVO> getLatestUploadBatches(Integer limit) {
        List<LatestUploadBatchVO> list = batchMapper.selectLatestBatches(limit);
        if (list == null) {
            return Collections.emptyList();
        }
        // 映射 statusText
        for (LatestUploadBatchVO vo : list) {
            vo.setStatusText(mapStatusText(vo.getStatus()));
        }
        return list;
    }

    // ========== 辅助方法 ==========

    /**
     * 补齐空日期：对于没有数据的日期，填充 count=0
     */
    private List<TrendItemVO> fillZeroDates(List<TrendItemVO> dbData, int days) {
        // 将 DB 数据转为 Map<date, count>
        Map<String, Long> dataMap = new LinkedHashMap<>();
        if (dbData != null) {
            for (TrendItemVO item : dbData) {
                dataMap.put(item.getDate(), item.getCount());
            }
        }

        // 生成从 (今天 - days + 1) 到今天的日期列表
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        List<TrendItemVO> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(fmt);
            TrendItemVO vo = new TrendItemVO();
            vo.setDate(dateStr);
            vo.setCount(dataMap.getOrDefault(dateStr, 0L));
            result.add(vo);
        }
        return result;
    }

    /**
     * status → statusText 映射
     */
    private String mapStatusText(Integer status) {
        if (status == null) return "未知状态";
        switch (status) {
            case 1: return "全部成功";
            case 2: return "部分成功";
            case 3: return "全部失败";
            default: return "未知状态";
        }
    }
}
