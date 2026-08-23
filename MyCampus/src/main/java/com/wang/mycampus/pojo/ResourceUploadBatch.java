package com.wang.mycampus.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件夹上传批次记录
 */
@Data
public class ResourceUploadBatch {
    private Long id;
    private String batchNo;
    private Long userId;
    private String folderName;
    private String description;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private Long totalSize;
    /** 0-处理中 1-全部成功 2-部分成功 3-全部失败 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
