package com.wang.mycampus.mapper;

import com.wang.mycampus.pojo.ResourceUploadBatch;
import com.wang.mycampus.vo.FolderUploadStatVO;
import com.wang.mycampus.vo.LatestUploadBatchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件夹上传批次 Mapper
 */
@Mapper
public interface ResourceUploadBatchMapper {

    /**
     * 插入批次记录
     */
    int insertBatch(ResourceUploadBatch batch);

    /**
     * 根据批次号查询批次
     */
    ResourceUploadBatch selectByBatchNo(@Param("batchNo") String batchNo);

    /**
     * 更新批次的成功/失败统计和最终状态
     */
    int updateBatchResult(@Param("batchNo") String batchNo,
                          @Param("successCount") Integer successCount,
                          @Param("failCount") Integer failCount,
                          @Param("status") Integer status);

    /**
     * 查询今日最大批次号，用于生成下一个批次序号
     */
    String selectMaxBatchNoForToday(@Param("prefix") String prefix);

    /**
     * 文件夹上传统计（按 status 分组）
     */
    FolderUploadStatVO selectBatchStats();

    /**
     * 最新上传批次列表
     */
    List<LatestUploadBatchVO> selectLatestBatches(@Param("limit") Integer limit);
}
