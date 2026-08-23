package com.wang.mycampus.vo;

import lombok.Data;

/**
 * 最新上传批次 VO
 */
@Data
public class LatestUploadBatchVO {
    private String batchNo;
    private String folderName;
    private String uploaderName;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private Long totalSize;
    private Integer status;
    private String statusText;
    private String createTime;
}
