package com.wang.mycampus.vo;

import lombok.Data;

import java.util.List;

@Data
public class CommentItemVO {
    private Long id;
    private Long postId;
    private Long userId;          // 顶层评论作者ID（前端回复该评论时用于 replyToUserId）
    private String content;
    private String authorName;
    private String authorAvatar;  // 评论者头像
    private String createTime;
    private Boolean isOwner;      // 当前登录用户是否本人（前端控制删除按钮）
    private List<CommentReplyItemVO> children; // 楼中楼回复，无回复时为空
}
