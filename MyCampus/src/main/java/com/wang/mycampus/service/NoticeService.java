package com.wang.mycampus.service;

import com.wang.mycampus.dto.NoticeDTO;
import com.wang.mycampus.vo.NoticeVO;
import com.wang.mycampus.vo.PageVO;

import java.util.List;

public interface NoticeService {

    /*
     * 前台：所有启用公告，按创建时间倒序（最新在前）
     * */
    List<NoticeVO> listActive();

    /*
     * 后台：分页 + 条件查询（keyword 标题模糊 / status 状态）
     * */
    PageVO<NoticeVO> adminPage(Integer page, Integer size, String keyword, Integer status);

    /*
     * 后台：公告详情
     * */
    NoticeVO getById(Long id);

    /*
     * 后台：新增公告，返回新ID
     * */
    Long add(NoticeDTO dto);

    /*
     * 后台：编辑公告（含启用/停用切换），强制刷新 update_time 版本号
     * */
    void update(Long id, NoticeDTO dto);

    /*
     * 后台：删除公告
     * */
    void delete(Long id);
}
