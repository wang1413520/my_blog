package com.wang.mycampus.vo;

import lombok.Data;

@Data
public class PostItemVO {
    private Long id;
    private String title;
    private String content;
    private Integer type;
    private Integer isAnonymous;
    private String authorName;  // 匿名则为"匿名用户"
    private String authorAvatar; // 作者头像，匿名时为null
    private Integer likeCount;
    private Integer commentCount;
    private String createTime;
}
