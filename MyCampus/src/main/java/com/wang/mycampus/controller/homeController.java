package com.wang.mycampus.controller;

import com.wang.mycampus.service.CommentService;
import com.wang.mycampus.service.PostService;
import com.wang.mycampus.service.ResourceService;
import com.wang.mycampus.vo.Result;
import com.wang.mycampus.vo.homeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping
public class homeController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ResourceService resourceService;

    @GetMapping("/api/home/stats")
    public Result<homeVO> getData(){
        log.info("获取首页的统计数据...");
        Integer postNum = postService.selectPostNumber();
        Integer commentNum = commentService.selectCommentNumber();
        Integer resourceNum = resourceService.selectResourceNumber();
        return Result.success(new homeVO(postNum,commentNum,resourceNum));
    }

}
