package com.wang.mycampus.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wang.mycampus.dto.NoticeDTO;
import com.wang.mycampus.exception.BaseException;
import com.wang.mycampus.mapper.NoticeMapper;
import com.wang.mycampus.pojo.Notice;
import com.wang.mycampus.service.NoticeService;
import com.wang.mycampus.vo.NoticeVO;
import com.wang.mycampus.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class NoticeServiceImpl implements NoticeService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private NoticeMapper noticeMapper;

    /*
     * 前台：所有启用公告，最新在前
     * */
    @Override
    public List<NoticeVO> listActive() {
        return noticeMapper.selectActive();
    }

    /*
     * 后台：分页 + 条件查询
     * */
    @Override
    public PageVO<NoticeVO> adminPage(Integer page, Integer size, String keyword, Integer status) {
        Page<Object> objects = PageHelper.startPage(page, size);
        List<NoticeVO> list = noticeMapper.selectPage(keyword, status);

        PageVO<NoticeVO> pageVO = new PageVO<>();
        pageVO.setRecords(list);
        pageVO.setTotal((int) objects.getTotal());
        pageVO.setPage(page);
        pageVO.setSize(size);
        return pageVO;
    }

    /*
     * 后台：公告详情
     * */
    @Override
    public NoticeVO getById(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BaseException(404, "公告不存在");
        }
        return toVO(notice);
    }

    /*
     * 后台：新增公告
     * status 缺省默认 1（启用）；create_time / update_time 显式赋值，保证版本号存在
     * */
    @Override
    public Long add(NoticeDTO dto) {
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        LocalDateTime now = LocalDateTime.now();
        notice.setCreateTime(now);
        notice.setUpdateTime(now);
        noticeMapper.insert(notice);
        return notice.getId();
    }

    /*
     * 后台：编辑公告（含启用/停用切换）
     * 动态 SQL 只更新非空字段；update_time 在 XML 中固定刷新为 NOW()，保证产生新版本
     * 支持"仅传 status"的停用/启用操作：title/content 为 null 时校验并保留原值
     * */
    @Override
    public void update(Long id, NoticeDTO dto) {
        // 手动校验：仅当字段非空时才校验，允许部分更新（如只传 status）
        if (dto.getTitle() != null && (dto.getTitle().trim().isEmpty() || dto.getTitle().length() > 100)) {
            throw new BaseException(400, "标题不能为空且不能超过100字符");
        }
        if (dto.getContent() != null && dto.getContent().length() > 2000) {
            throw new BaseException(400, "内容不能超过2000字符");
        }

        Notice notice = new Notice();
        notice.setId(id);
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setStatus(dto.getStatus());
        int updated = noticeMapper.update(notice);
        if (updated == 0) {
            throw new BaseException(404, "公告不存在");
        }
    }

    /*
     * 后台：删除公告
     * */
    @Override
    public void delete(Long id) {
        int deleted = noticeMapper.deleteById(id);
        if (deleted == 0) {
            throw new BaseException(404, "公告不存在");
        }
    }

    /*
     * 实体 → VO（时间格式化为 yyyy-MM-dd HH:mm:ss，保证前端版本对比稳定）
     * */
    private NoticeVO toVO(Notice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setStatus(notice.getStatus());
        vo.setCreateTime(notice.getCreateTime() != null ? notice.getCreateTime().format(FMT) : null);
        vo.setUpdateTime(notice.getUpdateTime() != null ? notice.getUpdateTime().format(FMT) : null);
        return vo;
    }
}
