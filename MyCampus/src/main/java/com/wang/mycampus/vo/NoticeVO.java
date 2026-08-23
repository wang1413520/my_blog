package com.wang.mycampus.vo;

import lombok.Data;

/**
 * 公告 VO
 * createTime / updateTime 使用 String（yyyy-MM-dd HH:mm:ss），
 * 保证前端"版本号"字符串直接比较稳定（见开发文档注意事项第5条）。
 */
@Data
public class NoticeVO {
    private Long id;
    private String title;
    private String content;
    private Integer status;
    private String createTime;
    private String updateTime;   // 前端版本号，必须返回
}
