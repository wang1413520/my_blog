
package com.wang.mycampus.controller;

import com.wang.mycampus.Utils.UserContext;
import com.wang.mycampus.annotation.RequireLogin;
import com.wang.mycampus.dto.PageDTO;
import com.wang.mycampus.dto.PublishPostDTO;
import com.wang.mycampus.service.PostService;
import com.wang.mycampus.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("")
public class PostController {

    @Autowired
    private PostService postService;

    /*
    * 插入一条帖子
    * */
    @RequireLogin
    @PostMapping("/api/post/publish")
    public Result<PostInsertVO> insertOnePost(@RequestBody PublishPostDTO publishPostDTO){
        log.info("正在插入一条帖子...{}",publishPostDTO);
        PostInsertVO postInsertVO = postService.insertOnePost(publishPostDTO);
        return Result.success(postInsertVO);
    }

    /*
    * （分页查询）获取帖子列表
    *
    * */

    @RequireLogin
    @GetMapping("/api/post/list")
    public Result<PageVO<PostItemVO>> selectPagePost(@RequestParam(required = false) Integer type,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size){
        log.info("分页查询 controller 接口...");
        PageDTO pageDTO = new PageDTO(type,page,size);
        PageVO<PostItemVO> pageVO = postService.selectPageSize(pageDTO);
        return Result.success(pageVO);
    }

    /*
     * 3.3 获取帖子详情
     * */
    @GetMapping("/api/post/{id}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long id) {
        log.info("获取帖子详情 id={}", id);
        PostDetailVO postDetail = postService.getPostDetail(id);
        return Result.success(postDetail);
    }

    /*
     * 3.4 点赞帖子
     * */
    @RequireLogin
    @PostMapping("/api/post/like/{id}")
    public Result<Void> likePost(@PathVariable Long id) {
        log.info("点赞帖子 id={}", id);
        Long userId = UserContext.getUserId();
        postService.likePost(id, userId);
        return Result.success();
    }

    /*
     * 3.5 删除帖子
     * */
    @RequireLogin
    @DeleteMapping("/api/post/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        log.info("删除帖子 id={}", id);
        Long userId = UserContext.getUserId();
        postService.deletePost(id, userId);
        return Result.success();
    }

    /*
     * 3.6 搜索帖子
     * */
    @GetMapping("/api/post/search")
    public Result<PageVO<PostItemVO>> searchPosts(@RequestParam String keyword,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        log.info("搜索帖子 keyword={}, page={}, size={}", keyword, page, size);
        PageVO<PostItemVO> pageVO = postService.searchPosts(keyword, page, size);
        return Result.success(pageVO);
    }


}
