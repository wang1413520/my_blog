package com.wang.mycampus.mapper;

import com.wang.mycampus.dto.PublishPostDTO;
import com.wang.mycampus.pojo.Post;
import com.wang.mycampus.vo.PostItemVO;
import com.wang.mycampus.vo.TrendItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostMapper {


    /*
    * 插入一条帖子
    * */
    Integer insertOnePost(@Param("publishPostDTO") PublishPostDTO publishPostDTO, @Param("userId") Long userId);


    /*
    * 分页查询
    * */
    List<PostItemVO> selectByPage(@Param("type") Integer type);

    /*
     * 根据ID查询帖子
     * */
    Post selectPostById(@Param("id") Long id);

    /*
     * 搜索帖子（标题和内容模糊匹配）
     * */
    List<PostItemVO> searchPosts(@Param("keyword") String keyword);

    /*
     * 根据帖子ID和用户ID删除帖子（仅作者可删除）
     * */
    int deletePostByIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    /*
     * 插入点赞记录
     * */
    int insertPostLike(@Param("postId") Long postId, @Param("userId") Long userId);

    /*
     * 检查是否已点赞
     * */
    int checkPostLiked(@Param("postId") Long postId, @Param("userId") Long userId);

    /*
     * 帖子点赞数 +1
     * */
    int incrementLikeCount(@Param("postId") Long postId);

    /*
     * 根据用户ID查询昵称
     * */
    @Select("SELECT nickname FROM user WHERE id = #{userId}")
    String selectNicknameByUserId(@Param("userId") Long userId);

    /*
     * 根据用户ID查询头像
     * */
    @Select("SELECT avatar FROM user WHERE id = #{userId}")
    String selectAvatarByUserId(@Param("userId") Long userId);

    /*
     * 根据用户ID查询用户名
     * */
    @Select("SELECT username FROM user WHERE id = #{userId}")
    String selectUsernameByUserId(@Param("userId") Long userId);

    /*
    * 查询全部的帖子数量
    * */
    @Select("select count(1) from post")
    Integer selectSumNumber();

    /*
     * 发帖趋势（近 N 天，按天分组）
     * */
    List<TrendItemVO> selectPostPublishTrend(@Param("days") Integer days);
}
