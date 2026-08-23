package com.wang.mycampus.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wang.mycampus.Utils.UserContext;
import com.wang.mycampus.annotation.RequireLogin;
import com.wang.mycampus.dto.PageDTO;
import com.wang.mycampus.dto.PublishPostDTO;
import com.wang.mycampus.exception.BaseException;
import com.wang.mycampus.mapper.CommentMapper;
import com.wang.mycampus.mapper.PostMapper;
import com.wang.mycampus.pojo.Post;
import com.wang.mycampus.service.CommentService;
import com.wang.mycampus.service.PostService;
import com.wang.mycampus.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@Service
public class PostServiceImpl implements PostService {


    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentService commentService;


    /*
    * 插入一条帖子（博客前端复用为发布文章）
    * */
    @RequireLogin
    @Override
    public PostInsertVO insertOnePost(PublishPostDTO publishPostDTO) {
        // 默认值兜底：博客前端可以不传 type 和 isAnonymous，兼容旧社区前端和新博客前端
        if (publishPostDTO.getType() == null) {
            publishPostDTO.setType(1);
        }
        if (publishPostDTO.getIsAnonymous() == null) {
            publishPostDTO.setIsAnonymous(0);
        }

        PostInsertVO postInsertVO = new PostInsertVO();
        BeanUtils.copyProperties(publishPostDTO,postInsertVO);
        // 获取帖子 id 插入帖子并给userId赋值
        Long userId = UserContext.getUserId();
        Integer postId = postMapper.insertOnePost(publishPostDTO,userId);
        postInsertVO.setId(postId);
        return postInsertVO;
    }


    /*
    * 分页查询帖子
    * */
    @Override
    public PageVO<PostItemVO> selectPageSize(PageDTO pageDTO) {
        // 获取分页参数
        int size = pageDTO.getSize();
        int page = pageDTO.getPage();
        Integer type = pageDTO.getType();
        // 启动分页（参数顺序：pageNum, pageSize）
        Page<Object> objects = PageHelper.startPage(page, size);
        List<PostItemVO> postItemVOList = postMapper.selectByPage(type);

        PageVO<PostItemVO> pageVO = new PageVO<>();
        pageVO.setRecords(postItemVOList);
        pageVO.setTotal((int) objects.getTotal());
        pageVO.setPage(page);
        pageVO.setSize(size);
        return pageVO;
    }

    /*
     * 获取帖子详情
     * */
    @Override
    public PostDetailVO getPostDetail(Long id) {
        // 查询帖子
        Post post = postMapper.selectPostById(id);
        if (post == null) {
            throw new BaseException(404, "帖子不存在");
        }

        PostDetailVO detailVO = new PostDetailVO();
        BeanUtils.copyProperties(post, detailVO);

        // 处理作者名称和头像
        if (post.getIsAnonymous() != null && post.getIsAnonymous() == 1) {
            detailVO.setAuthorName("匿名用户");
            detailVO.setAuthorAvatar(null);
        } else {
            String nickname = postMapper.selectNicknameByUserId(post.getUserId());
            String username = postMapper.selectUsernameByUserId(post.getUserId());
            String avatar = postMapper.selectAvatarByUserId(post.getUserId());
            // 优先使用昵称，没有则使用用户名
            detailVO.setAuthorName(nickname != null && !nickname.isEmpty() ? nickname : username);
            detailVO.setAuthorAvatar(avatar);
        }

        // 获取评论列表（含楼中楼 children 与 isOwner）
        List<CommentItemVO> comments = commentMapper.selectCommentsByPostId(post.getId());
        commentService.fillCommentTree(comments, post.getId(), UserContext.getUserId());
        detailVO.setComments(comments);

        // 格式化时间
        if (post.getCreateTime() != null) {
            detailVO.setCreateTime(post.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        return detailVO;
    }

    /*
     * 点赞帖子
     * */
    @Transactional
    @Override
    public void likePost(Long postId, Long userId) {
        // 检查帖子是否存在
        Post post = postMapper.selectPostById(postId);
        if (post == null) {
            throw new BaseException(404, "帖子不存在");
        }

        // 检查是否已点赞
        int liked = postMapper.checkPostLiked(postId, userId);
        if (liked > 0) {
            throw new BaseException(400, "您已经点过赞了");
        }

        // 插入点赞记录
        postMapper.insertPostLike(postId, userId);
        // 帖子点赞数 +1
        postMapper.incrementLikeCount(postId);
    }

    /*
     * 删除帖子（仅作者可删除）
     * */
    @Transactional
    @Override
    public void deletePost(Long postId, Long userId) {
        // 检查帖子是否存在
        Post post = postMapper.selectPostById(postId);
        if (post == null) {
            throw new BaseException(404, "帖子不存在");
        }

        // 检查是否是作者
        int deleted = postMapper.deletePostByIdAndUserId(postId, userId);
        if (deleted == 0) {
            throw new BaseException(403, "无权删除该帖子");
        }
    }

    /*
     * 搜索帖子
     * */
    @Override
    public PageVO<PostItemVO> searchPosts(String keyword, Integer page, Integer size) {
        Page<Object> objects = PageHelper.startPage(page, size);
        List<PostItemVO> postItemVOList = postMapper.searchPosts(keyword);

        PageVO<PostItemVO> pageVO = new PageVO<>();
        pageVO.setRecords(postItemVOList);
        pageVO.setTotal((int) objects.getTotal());
        pageVO.setPage(page);
        pageVO.setSize(size);
        return pageVO;
    }

    @Override
    public Integer selectPostNumber() {
        return postMapper.selectSumNumber();
    }
}
