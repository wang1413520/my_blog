package com.wang.mycampus.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wang.mycampus.Utils.UserContext;
import com.wang.mycampus.dto.LinkShareAddDTO;
import com.wang.mycampus.dto.LinkShareQueryDTO;
import com.wang.mycampus.dto.LinkShareUpdateStatusDTO;
import com.wang.mycampus.mapper.LinkShareMapper;
import com.wang.mycampus.mapper.UserMapper;
import com.wang.mycampus.pojo.LinkShare;
import com.wang.mycampus.service.LinkShareService;
import com.wang.mycampus.vo.LinkShareVO;
import com.wang.mycampus.vo.PageVO;
import com.wang.mycampus.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LinkShareServiceImpl implements LinkShareService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LinkShareMapper linkShareMapper;

    @Autowired
    private ObjectMapper objectMapper;


    /*
    * 查询链接资源列表
    *
    * */
    @Override
    public PageVO<LinkShareVO> selectList(LinkShareQueryDTO queryDTO) {
        Integer page = queryDTO.getPage();
        Integer size = queryDTO.getSize();
        // 模糊匹配标题，简介，来源，标签
        String keyword = queryDTO.getKeyword();
        // 按照来源名称筛选
        String sourceName = queryDTO.getSourceName();

        // 区分是普通用户还是管理员 根据 id 查询 role
        Long userId = UserContext.getUserId();
        Integer role = userMapper.selectRoleByUserId(userId);
        List<LinkShareVO> list;
        // 启动分页查询
        Page<Object> pageInfo = PageHelper.startPage(page, size);

        if(role == 1){
            // 查询 ---> 返回结果
            list = linkShareMapper.selectList(keyword, sourceName);
        }else{
            // 查询 ---> status == 1 没被禁用
            list = linkShareMapper.selectNoBanList(keyword,sourceName);
        }


        // 组装分页结果
        PageVO<LinkShareVO> pageVO = new PageVO<>();
        pageVO.setRecords(list);
        pageVO.setTotal((int) pageInfo.getTotal());
        pageVO.setPage(page);
        pageVO.setSize(size);

        return pageVO;
    }


    /*
    * 查询一个（内部用：管理员操作，不限制 status）
    * */
    @Override
    public LinkShareVO selectById(Long id) {
        return linkShareMapper.selectById(id);
    }

    /*
    * 查询一个（公开用：前台展示，只返回 status = 1）
    * */
    @Override
    public LinkShareVO selectByIdPublic(Long id) {
        return linkShareMapper.selectByIdPublic(id);
    }

    /*
    * 新增一个链接
    * */
    @Override
    public Result<LinkShareVO> insertOne(LinkShareAddDTO linkShareAddDTO) {
        // 1. 从 jwt 中获取 userId
        Long userId = UserContext.getUserId();

        // 2. DTO → POJO 转换
        LinkShare linkShare = new LinkShare();
        BeanUtils.copyProperties(linkShareAddDTO, linkShare);

        // 3. List<String> tags → JSON 字符串（如 ["视频","Vue3"] → "[\"视频\",\"Vue3\"]"）
        try {
            linkShare.setTags(objectMapper.writeValueAsString(linkShareAddDTO.getTags()));
        } catch (JsonProcessingException e) {
            log.error("tags 序列化失败", e);
            throw new RuntimeException("tags 格式错误", e);
        }

        linkShare.setCreatedBy(userId);

        // 4. 执行 insert（useGeneratedKeys 自动回写自增 id）
        linkShareMapper.insert(linkShare);

        // 5. 查询回显：用新 id 查完整 VO（含 createdByName 等联表字段）
        LinkShareVO vo = linkShareMapper.selectById(linkShare.getId());

        return Result.success(vo);
    }

    /*
    * 编辑链接
    * */
    @Override
    public Result<LinkShareVO> update(Long id, LinkShareAddDTO linkShareAddDTO) {
        // 1. 检查是否存在
        LinkShareVO existing = linkShareMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "链接不存在");
        }

        // 2. DTO → POJO 转换
        LinkShare linkShare = new LinkShare();
        BeanUtils.copyProperties(linkShareAddDTO, linkShare);
        try {
            linkShare.setTags(objectMapper.writeValueAsString(linkShareAddDTO.getTags()));
        } catch (JsonProcessingException e) {
            log.error("tags 序列化失败", e);
            throw new RuntimeException("tags 格式错误", e);
        }
        linkShare.setId(id);

        // 3. 执行更新
        linkShareMapper.updateById(linkShare);

        // 4. 查询回显
        return Result.success(linkShareMapper.selectById(id));
    }

    /*
    * 启用或禁用链接
    * */
    @Override
    public Result<Void> updateStatus(Long id, LinkShareUpdateStatusDTO statusDTO) {
        LinkShareVO existing = linkShareMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "链接不存在");
        }
        linkShareMapper.updateStatus(id, statusDTO.getStatus());
        return Result.success();
    }

    /*
    * 删除链接（物理删除）
    * */
    @Override
    public Result<Void> deleteById(Long id) {
        LinkShareVO existing = linkShareMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "链接不存在");
        }
        linkShareMapper.deleteById(id);
        return Result.success();
    }
}
