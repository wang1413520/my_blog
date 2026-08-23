package com.wang.mycampus.vo;

import lombok.Data;

/**
 * 文件夹上传中单个文件的返回 VO
 */
@Data
public class ResourceFolderItemVO {
    private Long id;
    private String title;
    private String fileType;
    private Long fileSize;
    private String folderName;
    private String relativePath;
    private String batchNo;
    private String createTime;
}
