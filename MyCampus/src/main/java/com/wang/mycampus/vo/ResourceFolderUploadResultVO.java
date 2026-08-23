package com.wang.mycampus.vo;

import lombok.Data;

import java.util.List;

/**
 * 文件夹上传结果 VO
 */
@Data
public class ResourceFolderUploadResultVO {
    private String batchNo;
    private String folderName;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private Long totalSize;
    /** 批次状态: 0-处理中 1-全部成功 2-部分成功 3-全部失败 */
    private Integer status;
    /** 上传成功的文件列表 */
    private List<ResourceFolderItemVO> records;
    /** 上传失败的文件明细 */
    private List<FailedUploadFileVO> failedFiles;
}
