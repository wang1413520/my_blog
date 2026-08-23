package com.wang.mycampus.controller;

import com.wang.mycampus.annotation.RequireAdmin;
import com.wang.mycampus.annotation.RequireLogin;
import com.wang.mycampus.dto.LinkShareAddDTO;
import com.wang.mycampus.dto.LinkShareQueryDTO;
import com.wang.mycampus.dto.LinkShareUpdateStatusDTO;
import com.wang.mycampus.service.LinkShareService;
import com.wang.mycampus.vo.LinkShareVO;
import com.wang.mycampus.vo.PageVO;
import com.wang.mycampus.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class LinkShareController {

    @Autowired
    private LinkShareService linkShareService;


    /*
    * 查询列表
    * */
    @RequireLogin
    @GetMapping("/api/link-share/list")
    public Result<PageVO<LinkShareVO>> selectList(LinkShareQueryDTO queryDTO){
        log.info("用 {} 查询链接资源...", queryDTO);
        PageVO<LinkShareVO> pageVO = linkShareService.selectList(queryDTO);
        return Result.success(pageVO);
    }

    /*
    * 查看链接详情（公开：只返回 status = 1 的数据）
    * */
    @RequireLogin
    @GetMapping("/api/link-share/{id}")
    public Result<LinkShareVO> selectByLinkId(@PathVariable Long id){
        log.info("查询链接详情, id: {}", id);
        LinkShareVO vo = linkShareService.selectByIdPublic(id);
        if (vo == null) {
            return Result.error(404, "链接不存在或已禁用");
        }
        return Result.success(vo);
    }

    /*
    * 新增链接 ---> 管理员端
    *
    * */
    @RequireLogin
    @RequireAdmin
    @PostMapping("/api/link-share")
    public Result<LinkShareVO> insertLinkShare(@RequestBody LinkShareAddDTO linkShareAddDTO){
        log.info("新增一条链接 :{}",linkShareAddDTO);
        return linkShareService.insertOne(linkShareAddDTO);
    }

    /*
    * 编辑链接
    * */
    @RequireLogin
    @PutMapping("/api/link-share/{id}")
    @RequireAdmin
    public Result<LinkShareVO> updateLinkShare(@PathVariable Long id,
                                                @RequestBody LinkShareAddDTO linkShareAddDTO){
        log.info("编辑链接, id: {}", id);
        return linkShareService.update(id, linkShareAddDTO);
    }

    /*
    * 启用或禁用链接
    * */
    @RequireLogin
    @PatchMapping("/api/link-share/{id}/status")
    @RequireAdmin
    public Result<Void> updateLinkStatus(@PathVariable Long id,
                                          @RequestBody @Valid LinkShareUpdateStatusDTO statusDTO){
        log.info("更新链接状态, id: {}, status: {}", id, statusDTO.getStatus());
        return linkShareService.updateStatus(id, statusDTO);
    }

    /*
    * 删除链接（物理删除）
    * */
    @RequireLogin
    @DeleteMapping("/api/link-share/{id}")
    @RequireAdmin
    public Result<Void> deleteLinkShare(@PathVariable Long id){
        log.info("删除链接, id: {}", id);
        return linkShareService.deleteById(id);
    }

}
