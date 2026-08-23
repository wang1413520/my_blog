package com.wang.mycampus.controller;

import com.wang.mycampus.Utils.JWT;
import com.wang.mycampus.Utils.UserContext;
import com.wang.mycampus.annotation.RequireAdmin;
import com.wang.mycampus.dto.FeaturedResourceAddDTO;
import com.wang.mycampus.dto.FeaturedResourceQueryDTO;
import com.wang.mycampus.dto.FeaturedResourceUpdateDTO;
import com.wang.mycampus.service.FeaturedResourceService;
import com.wang.mycampus.vo.FeaturedResourceVO;
import com.wang.mycampus.vo.PageVO;
import com.wang.mycampus.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class FeaturedResourceController {


    @Autowired
    private FeaturedResourceService featuredResourceService;

    /*
    * 查询站长的主推列表
    * */
    @GetMapping("/api/featured-resource/list")
    public Result<PageVO<FeaturedResourceVO>> selectFeatureList(FeaturedResourceQueryDTO featuredResourceQueryDTO){
        log.info("查询站长的主推列表...");
        PageVO<FeaturedResourceVO> pageVO = featuredResourceService.selectList(featuredResourceQueryDTO);
        return Result.success(pageVO);
    }

    /*
    * 新增
    * */
    @RequireAdmin
    @PostMapping("/api/featured-resource")
    public Result<Void> addFeaResource(@RequestBody FeaturedResourceAddDTO featuredResourceAddDTO){
        Long userId = UserContext.getUserId();
        featuredResourceService.insertOne(userId,featuredResourceAddDTO);
        return Result.success();

    }

    /*
    * 更新
    * */
    @RequireAdmin
    @PutMapping("/api/featured-resource/{id}")
    public Result<Void> update(@PathVariable Long id , @RequestBody FeaturedResourceUpdateDTO featuredResourceUpdateDTO){
        log.info("更新主推 id为 : {}",id);
        featuredResourceService.update(id,featuredResourceUpdateDTO);
        return Result.success();
    }

    /*
    * 删除
    * */
    @RequireAdmin
    @DeleteMapping("/api/featured-resource/{id}")
    public Result<Void> deleteById(@PathVariable Long id){
        log.info("删除主推帖子为: {} " , id);
        featuredResourceService.deleteById(id);
        return Result.success();
    }

    /*
    * 后台查看
    * */
    @RequireAdmin
    @GetMapping("/api/featured-resource/admin/list")
    public Result<PageVO<FeaturedResourceVO>> getAdminFeaturedList(FeaturedResourceQueryDTO queryDTO) {
        return Result.success(featuredResourceService.getAdminFeaturedList(queryDTO));
    }


    /*
    * 查看详情（查看按钮）
    * */
    @RequireAdmin
    @GetMapping("/api/featured-resource/{id}")
    public Result<FeaturedResourceVO> getDetail(@PathVariable Long id) {
        return Result.success(featuredResourceService.getDetail(id));
    }



}
