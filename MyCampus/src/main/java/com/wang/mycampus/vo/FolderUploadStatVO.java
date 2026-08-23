package com.wang.mycampus.vo;

import lombok.Data;

/**
 * 文件夹上传成功/失败统计 VO
 */
@Data
public class FolderUploadStatVO {
    private Long totalBatchCount;
    private Long successBatchCount;
    private Long partialSuccessBatchCount;
    private Long failedBatchCount;
}
