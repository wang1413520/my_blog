package com.wang.mycampus.service;


import com.wang.mycampus.dto.PageDTO;
import com.wang.mycampus.dto.PublishPostDTO;
import com.wang.mycampus.vo.PageVO;
import com.wang.mycampus.vo.PostDetailVO;
import com.wang.mycampus.vo.PostInsertVO;
import com.wang.mycampus.vo.PostItemVO;

public interface PostService {

    /*
    * 插入一条帖子
    * */
    PostInsertVO insertOnePost(PublishPostDTO publishPostDTO);

    /*
    * 分页查询帖子
    * */
    PageVO<PostItemVO> selectPageSize(PageDTO pageDTO);

    /*
     * 获取帖子详情（含评论列表）
     * */
    PostDetailVO getPostDetail(Long id);

    /*
     * 点赞帖子
     * */
    void likePost(Long postId, Long userId);

    /*
     * 删除帖子
     * */
    void deletePost(Long postId, Long userId);

    /*
     * 搜索帖子
     * */
    PageVO<PostItemVO> searchPosts(String keyword, Integer page, Integer size);

    /*
    * 查询帖子的总数量
    * */
    Integer selectPostNumber();

}
