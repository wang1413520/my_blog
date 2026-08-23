package com.wang.mycampus.mapper;

import com.wang.mycampus.dto.AddCommentDTO;
import com.wang.mycampus.pojo.Comment;
import com.wang.mycampus.pojo.CommentReply;
import com.wang.mycampus.vo.CommentItemVO;
import com.wang.mycampus.vo.CommentReplyItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {

    /*
     * 发表评论
     * */
    int insertComment(@Param("postId") Long postId,
                      @Param("userId") Long userId,
                      @Param("content") String content);

    /*
     * 发表评论（回填自增ID，用于返回新记录ID）
     * */
    int insertCommentWithId(@Param("dto") AddCommentDTO addCommentDTO,
                            @Param("userId") Long userId);

    /*
     * 获取帖子的评论列表
     * */
    List<CommentItemVO> selectCommentsByPostId(@Param("postId") Long postId);

    /*
     * 根据评论ID和用户ID删除评论（仅作者可删除）
     * */
    int deleteCommentByIdAndUserId(@Param("commentId") Long commentId,
                                   @Param("userId") Long userId);

    /*
     * 根据评论ID删除评论（管理员）
     * */
    int deleteCommentById(@Param("commentId") Long commentId);

    /*
     * 查询评论的作者ID
     * */
    @Select("SELECT user_id FROM comment WHERE id = #{id}")
    Long selectUserIdByCommentId(@Param("id") Long id);

    /*
     * 查询顶层评论（被回复时用于确认目标、取作者ID）
     * */
    @Select("SELECT * FROM comment WHERE id = #{id}")
    Comment selectCommentById(@Param("id") Long id);

    /*
    * 查询所有帖子的数量
    * */
    @Select("select count(1) from comment;")
    Integer selectNumber();

    // ==================== 楼中楼（comment_reply 表）====================

    /*
     * 插入楼中楼回复（回填自增ID）
     * */
    int insertCommentReply(CommentReply commentReply);

    /*
     * 根据ID查询回复（add 分流时校验目标、取 root_comment_id / 作者ID）
     * */
    @Select("SELECT * FROM comment_reply WHERE id = #{id}")
    CommentReply selectReplyById(@Param("id") Long id);

    /*
     * 一次 IN 查询取出某帖某批顶层评论下的全部有效回复（含作者、被@用户名）
     * */
    List<CommentReplyItemVO> selectRepliesByPostIdAndRootIds(@Param("postId") Long postId,
                                                             @Param("rootIds") List<Long> rootIds);

    /*
     * 查询回复的作者ID（仅有效回复，软删后不可再删）
     * */
    @Select("SELECT user_id FROM comment_reply WHERE id = #{id} AND status = 1")
    Long selectUserIdByReplyId(@Param("id") Long id);

    /*
     * 软删回复（作者）
     * */
    int deleteCommentReplyByIdAndUserId(@Param("replyId") Long replyId,
                                        @Param("userId") Long userId);

    /*
     * 软删回复（管理员）
     * */
    int deleteCommentReplyById(@Param("replyId") Long replyId);

    /*
     * 删除顶层评论时级联清理其下所有回复（物理删除，避免孤儿数据）
     * */
    int deleteRepliesByRootCommentId(@Param("rootCommentId") Long rootCommentId);

}
