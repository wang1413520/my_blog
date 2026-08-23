package com.wang.mycampus.controller;

import com.wang.mycampus.Utils.JWT;
import com.wang.mycampus.Utils.UserContext;
import com.wang.mycampus.annotation.RequireLogin;
import com.wang.mycampus.dto.AddCommentDTO;
import com.wang.mycampus.service.CommentService;
import com.wang.mycampus.vo.CommentItemVO;
import com.wang.mycampus.vo.PageVO;
import com.wang.mycampus.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /*
     * 4.1 发表评论（parentId 为空 = 评论文章走原表；有值 = 楼中楼回复，返回新记录ID）
     * */
    @RequireLogin
    @PostMapping("/api/comment/add")
    public Result<Long> addComment(@RequestBody AddCommentDTO addCommentDTO) {
        log.info("发表评论 postId={}, content={}, parentId={}, replyToUserId={}",
                addCommentDTO.getPostId(), addCommentDTO.getContent(),
                addCommentDTO.getParentId(), addCommentDTO.getReplyToUserId());
        Long userId = UserContext.getUserId();
        Long commentId = commentService.addComment(userId, addCommentDTO);
        return Result.success(commentId);
    }

    /*
     * 4.2 获取评论列表（分页，顶层带 children 楼中楼）
     * 该接口公开可看；若请求头带有效 Token 则识别当前用户用于 isOwner，未登录/无效 Token 则 isOwner 全为 false
     * */
    @GetMapping("/api/comment/list")
    public Result<PageVO<CommentItemVO>> getCommentList(@RequestParam Long postId,
                                                        @RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "20") Integer size,
                                                        HttpServletRequest request) {
        log.info("获取评论列表 postId={}, page={}, size={}", postId, page, size);
        Long currentUserId = parseCurrentUserId(request);
        PageVO<CommentItemVO> pageVO = commentService.getCommentList(postId, page, size, currentUserId);
        return Result.success(pageVO);
    }

    /*
     * 4.3 删除评论（顶层，同时级联删除其下楼中楼）
     * */
    @RequireLogin
    @DeleteMapping("/api/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        log.info("删除评论 id={}", id);
        Long userId = UserContext.getUserId();
        commentService.deleteComment(id, userId);
        return Result.success();
    }

    /*
     * 4.4 删除楼中楼回复（软删，仅本人或管理员）
     * */
    @RequireLogin
    @DeleteMapping("/api/comment/reply/{id}")
    public Result<Void> deleteCommentReply(@PathVariable Long id) {
        log.info("删除楼中楼回复 id={}", id);
        Long userId = UserContext.getUserId();
        commentService.deleteCommentReply(id, userId);
        return Result.success();
    }

    /*
     * 公开接口里解析当前登录用户（list 无 @RequireLogin，拦截器不会设置 UserContext）
     * token 缺失/无效时视为未登录，返回 null
     * */
    private Long parseCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        String token = authHeader;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        token = token.trim();
        if (token.isEmpty()) {
            return null;
        }
        try {
            return JWT.getUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}
