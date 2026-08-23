package com.wang.mycampus.controller;

import com.wang.mycampus.annotation.RequireAdmin;
import com.wang.mycampus.dto.NoticeDTO;
import com.wang.mycampus.service.NoticeService;
import com.wang.mycampus.vo.NoticeVO;
import com.wang.mycampus.vo.PageVO;
import com.wang.mycampus.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /*
     * 前台公开接口：启用的公告列表（最新在前），无需登录
     * */
    @GetMapping("/api/notice/list")
    public Result<List<NoticeVO>> listActive() {
        log.info("前台获取公告列表");
        return Result.success(noticeService.listActive());
    }

    /*
     * 后台：分页列表，需管理员
     * */
    @RequireAdmin
    @GetMapping("/api/notice/admin/list")
    public Result<PageVO<NoticeVO>> adminPage(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status) {
        log.info("后台公告列表 page={}, size={}, keyword={}, status={}", page, size, keyword, status);
        return Result.success(noticeService.adminPage(page, size, keyword, status));
    }

    /*
     * 后台：公告详情，需管理员
     * */
    @RequireAdmin
    @GetMapping("/api/notice/{id}")
    public Result<NoticeVO> get(@PathVariable Long id) {
        log.info("后台公告详情 id={}", id);
        return Result.success(noticeService.getById(id));
    }

    /*
     * 后台：新增公告，需管理员
     * */
    @RequireAdmin
    @PostMapping("/api/notice")
    public Result<Long> add(@RequestBody @Valid NoticeDTO dto) {
        log.info("后台新增公告 title={}, status={}", dto.getTitle(), dto.getStatus());
        return Result.success(noticeService.add(dto));
    }

    /*
     * 后台：编辑公告（含启用/停用），需管理员，会刷新 update_time 版本号
     * 注意：不用 @Valid —— 支持"仅传 status"的停用/启用操作（title/content 可省略，省略则保留原值）
     */
    @RequireAdmin
    @PutMapping("/api/notice/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody NoticeDTO dto) {
        log.info("后台编辑公告 id={}, status={}", id, dto.getStatus());
        noticeService.update(id, dto);
        return Result.success();
    }

    /*
     * 后台：删除公告，需管理员
     * */
    @RequireAdmin
    @DeleteMapping("/api/notice/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("后台删除公告 id={}", id);
        noticeService.delete(id);
        return Result.success();
    }
}
