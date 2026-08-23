package com.wang.mycampus.vo;

import lombok.Data;

/**
 * 上传失败文件明细 VO
 */
@Data
public class FailedUploadFileVO {
    private String fileName;
    private String relativePath;
    private String reason;
}
