package com.wang.mycampus.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wang.mycampus.dto.AddCommentDTO;
import com.wang.mycampus.exception.BaseException;
import com.wang.mycampus.mapper.CommentMapper;
import com.wang.mycampus.mapper.PostMapper;
import com.wang.mycampus.mapper.UserMapper;
import com.wang.mycampus.pojo.Comment;
import com.wang.mycampus.pojo.CommentReply;
import com.wang.mycampus.pojo.Post;
import com.wang.mycampus.service.CommentService;
import com.wang.mycampus.vo.CommentItemVO;
import com.wang.mycampus.vo.CommentReplyItemVO;
import com.wang.mycampus.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    /**
     * 单个顶层评论下楼中楼回复展示上限（超出截断，前端二期可做"查看全部回复"展开）
     */
    private static final int MAX_REPLY_PER_ROOT = 50;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    /*
     * 发表评论：parentId 为空 → 顶层评论（原 comment 表）；有值 → 楼中楼（comment_reply 表）
     * */
    @Override
    public Long addComment(Long userId, AddCommentDTO addCommentDTO) {
        // 检查帖子是否存在
        Post post = postMapper.selectPostById(addCommentDTO.getPostId());
        if (post == null) {
            throw new BaseException(404, "帖子不存在");
        }

        // 校验评论内容长度
        String content = addCommentDTO.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new BaseException(400, "评论内容不能为空");
        }
        if (content.length() > 500) {
            throw new BaseException(400, "评论内容不能超过500字");
        }
        content = content.trim();

        // 分支：无 parentId = 直接评论文章，走原 comment 表
        if (addCommentDTO.getParentId() == null) {
            commentMapper.insertCommentWithId(addCommentDTO, userId);
            return addCommentDTO.getId();
        }

        // 有 parentId = 走楼中楼
        return addReply(userId, addCommentDTO, post.getId(), content);
    }

    /*
     * 楼中楼写入逻辑：
     * 1. 先查 comment_reply（回复楼中楼）；查不到再回退查 comment（回复顶层评论），两者都查不到报错
     * 2. 无论哪种，都要先校验目标存在、再比对 postId（防串楼）
     * 3. root_comment_id 一律取自被回复记录，绝不信任前端
     * */
    private Long addReply(Long userId, AddCommentDTO addCommentDTO, Long postId, String content) {
        CommentReply entity = new CommentReply();
        entity.setPostId(postId);
        entity.setContent(content);
        entity.setUserId(userId);

        // ① 回复楼中楼里的某条回复（parentId = comment_reply.id）
        CommentReply targetReply = commentMapper.selectReplyById(addCommentDTO.getParentId());
        if (targetReply != null) {
            checkNoCrossPost(targetReply.getPostId(), postId);
            entity.setRootCommentId(targetReply.getRootCommentId());
            entity.setParentId(addCommentDTO.getParentId());
            entity.setReplyToUserId(addCommentDTO.getReplyToUserId() != null
                    ? addCommentDTO.getReplyToUserId() : targetReply.getUserId());
        } else {
            // ② 回退：直接回复顶层评论（parentId = comment.id），parent_id 置 NULL
            Comment targetComment = commentMapper.selectCommentById(addCommentDTO.getParentId());
            if (targetComment == null) {
                throw new BaseException(404, "回复的评论不存在");
            }
            checkNoCrossPost(targetComment.getPostId(), postId);
            entity.setRootCommentId(targetComment.getId());
            entity.setParentId(null);
            entity.setReplyToUserId(addCommentDTO.getReplyToUserId() != null
                    ? addCommentDTO.getReplyToUserId() : targetComment.getUserId());
        }

        // 校验被@用户真实存在，防止伪造 @
        if (entity.getReplyToUserId() != null && userMapper.selectById(entity.getReplyToUserId()) == null) {
            throw new BaseException(400, "被回复的用户不存在");
        }

        commentMapper.insertCommentReply(entity);
        return entity.getId();
    }

    /*
     * 防串楼：被回复记录必须属于同一帖子
     * */
    private void checkNoCrossPost(Long targetPostId, Long requestPostId) {
        if (!Objects.equals(targetPostId, requestPostId)) {
            throw new BaseException(400, "不能跨帖子回复");
        }
    }

    /*
     * 获取评论列表（分页）：顶层走原分页 SQL，楼中楼一次 IN 查询带回后按 root 分组挂 children
     * */
    @Override
    public PageVO<CommentItemVO> getCommentList(Long postId, Integer page, Integer size, Long currentUserId) {
        // 检查帖子是否存在
        Post post = postMapper.selectPostById(postId);
        if (post == null) {
            throw new BaseException(404, "帖子不存在");
        }

        Page<Object> objects = PageHelper.startPage(page, size);
        List<CommentItemVO> commentList = commentMapper.selectCommentsByPostId(postId);

        // 组装楼中楼 children + isOwner
        fillCommentTree(commentList, postId, currentUserId);

        PageVO<CommentItemVO> pageVO = new PageVO<>();
        pageVO.setRecords(commentList);
        pageVO.setTotal((int) objects.getTotal());
        pageVO.setPage(page);
        pageVO.setSize(size);
        return pageVO;
    }

    /*
     * 删除顶层评论：作者或管理员可删；同时级联清理该顶层评论下全部楼中楼回复（防孤儿数据）
     * */
    @Override
    public void deleteComment(Long commentId, Long userId) {
        // 检查评论是否存在
        Long authorId = commentMapper.selectUserIdByCommentId(commentId);
        if (authorId == null) {
            throw new BaseException(404, "评论不存在");
        }

        if (isAdmin(userId)) {
            commentMapper.deleteCommentById(commentId);
        } else {
            // 仅评论者可删除
            int deleted = commentMapper.deleteCommentByIdAndUserId(commentId, userId);
            if (deleted == 0) {
                throw new BaseException(403, "无权删除该评论");
            }
        }

        // 级联清理楼中楼回复
        commentMapper.deleteRepliesByRootCommentId(commentId);
    }

    /*
     * 删除楼中楼回复：作者或管理员可删，软删（status=0），不影响同楼其他回复
     * */
    @Override
    public void deleteCommentReply(Long replyId, Long userId) {
        // 检查回复是否存在
        Long authorId = commentMapper.selectUserIdByReplyId(replyId);
        if (authorId == null) {
            throw new BaseException(404, "回复不存在");
        }

        if (isAdmin(userId)) {
            commentMapper.deleteCommentReplyById(replyId);
        } else {
            int deleted = commentMapper.deleteCommentReplyByIdAndUserId(replyId, userId);
            if (deleted == 0) {
                throw new BaseException(403, "无权删除该回复");
            }
        }
    }

    @Override
    public Integer selectCommentNumber() {
        return commentMapper.selectNumber();
    }

    /*
     * 组装楼中楼 children 与 isOwner，供评论列表与帖子详情复用
     * */
    @Override
    public void fillCommentTree(List<CommentItemVO> topComments, Long postId, Long currentUserId) {
        if (topComments == null || topComments.isEmpty()) {
            return;
        }

        // 顶层 isOwner
        for (CommentItemVO c : topComments) {
            c.setIsOwner(currentUserId != null && currentUserId.equals(c.getUserId()));
        }

        List<Long> rootIds = topComments.stream()
                .map(CommentItemVO::getId)
                .collect(Collectors.toList());

        // 一次 IN 查询带回全部有效回复，避免 N+1
        List<CommentReplyItemVO> replies = commentMapper.selectRepliesByPostIdAndRootIds(postId, rootIds);
        if (replies == null || replies.isEmpty()) {
            for (CommentItemVO c : topComments) {
                c.setChildren(new ArrayList<>());
            }
            return;
        }

        // 按 root_comment_id 分组，LinkedHashMap 保持楼层顺序
        Map<Long, List<CommentReplyItemVO>> groupMap = replies.stream()
                .collect(Collectors.groupingBy(CommentReplyItemVO::getRootCommentId,
                        LinkedHashMap::new, Collectors.toList()));

        for (CommentItemVO c : topComments) {
            List<CommentReplyItemVO> children = groupMap.get(c.getId());
            if (children == null || children.isEmpty()) {
                c.setChildren(new ArrayList<>());
                continue;
            }
            // 单顶层回复上限截断
            List<CommentReplyItemVO> limited = children.size() > MAX_REPLY_PER_ROOT
                    ? new ArrayList<>(children.subList(0, MAX_REPLY_PER_ROOT)) : children;
            for (CommentReplyItemVO r : limited) {
                r.setIsOwner(currentUserId != null && currentUserId.equals(r.getUserId()));
            }
            c.setChildren(limited);
        }
    }

    /*
     * 当前用户是否为管理员
     * */
    private boolean isAdmin(Long userId) {
        Integer role = userMapper.selectRoleByUserId(userId);
        return role != null && role == 1;
    }
}
