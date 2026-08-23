package com.wang.mycampus.vo;

import lombok.Data;

/**
 * 楼中楼回复 VO（挂在顶层评论的 children 下）
 */
@Data
public class CommentReplyItemVO {
    private Long id;
    private Long postId;
    private Long rootCommentId;
    private Long parentId;
    private Long userId;             // 回复人ID（前端回复该回复时需要，用于 replyToUserId）
    private Long replyToUserId;      // 被@的用户ID
    private String replyToUserName;  // 被@的人名（NULL 时前端回退为顶层评论作者名）
    private String authorName;       // 回复人名字
    private String authorAvatar;     // 回复人头像
    private String content;
    private String createTime;
    private Boolean isOwner;         // 当前登录用户是否本人（前端控制删除按钮）
}
