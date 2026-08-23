package com.wang.mycampus.vo;

import lombok.Data;

import java.util.List;

@Data
public class PostDetailVO {
    private Long id;
    private String title;
    private String content;
    private Integer type;
    private Integer isAnonymous;
    private String authorName;
    private String authorAvatar; // 作者头像，匿名时为null
    private Integer likeCount;
    private String createTime;
    private List<CommentItemVO> comments;

}
