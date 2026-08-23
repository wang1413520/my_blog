package com.wang.mycampus.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wang.mycampus.dto.FeaturedResourceAddDTO;
import com.wang.mycampus.dto.FeaturedResourceQueryDTO;
import com.wang.mycampus.dto.FeaturedResourceUpdateDTO;
import com.wang.mycampus.mapper.FeaturedResourceMapper;
import com.wang.mycampus.mapper.ResourceMapper;
import com.wang.mycampus.pojo.FeaturedResource;
import com.wang.mycampus.pojo.Resource;
import com.wang.mycampus.service.FeaturedResourceService;
import com.wang.mycampus.vo.FeaturedResourceVO;
import com.wang.mycampus.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeaturedResourceServiceImpl implements FeaturedResourceService {

    @Autowired
    private FeaturedResourceMapper featuredResourceMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Override
    public PageVO<FeaturedResourceVO> selectList(FeaturedResourceQueryDTO featuredResourceQueryDTO) {
        Integer page = featuredResourceQueryDTO.getPage();
        Integer size = featuredResourceQueryDTO.getSize();
        String keyword = featuredResourceQueryDTO.getKeyword();
        // 开启
        Page<Object> objects = PageHelper.startPage(page, size);
        List<FeaturedResourceVO> list = featuredResourceMapper.selectList(keyword);

        PageVO<FeaturedResourceVO> pageVO = new PageVO<>();
        pageVO.setPage(page);
        pageVO.setSize(size);
        pageVO.setRecords(list);
        pageVO.setTotal((int) objects.getTotal());

        return pageVO;

    }

    @Override
    public PageVO<FeaturedResourceVO> getAdminFeaturedList(FeaturedResourceQueryDTO queryDTO) {
        Integer page = queryDTO.getPage();
        Integer size = queryDTO.getSize();
        String keyword = queryDTO.getKeyword();
        Integer status = queryDTO.getStatus();

        Page<Object> objects = PageHelper.startPage(page, size);
        List<FeaturedResourceVO> list = featuredResourceMapper.selectAdminList(keyword, status);

        PageVO<FeaturedResourceVO> pageVO = new PageVO<>();
        pageVO.setPage(page);
        pageVO.setSize(size);
        pageVO.setRecords(list);
        pageVO.setTotal((int) objects.getTotal());

        return pageVO;
    }

    @Override
    public void insertOne(Long userId, FeaturedResourceAddDTO featuredResourceAddDTO) {
        //
        Resource resource = resourceMapper.selectResourceById(featuredResourceAddDTO.getResourceId());
        if(resource == null){
            throw new RuntimeException("该资源不存在");
        }

        //
        FeaturedResource featuredResource = featuredResourceMapper.selectByResourceId(featuredResourceAddDTO.getResourceId());
        if(featuredResource != null){
            throw new RuntimeException("该资源已经存在");
        }

        featuredResourceMapper.insertOne(userId,featuredResourceAddDTO);

    }

    @Override
    public void update(Long id, FeaturedResourceUpdateDTO featuredResourceUpdateDTO) {
        FeaturedResourceVO existing = featuredResourceMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("主推记录不存在");
        }
        featuredResourceMapper.updateById(id, featuredResourceUpdateDTO);
    }

    @Override
    public void deleteById(Long id) {
        featuredResourceMapper.deleteById(id);
    }

    @Override
    public FeaturedResourceVO getDetail(Long ResId) {
        // 两表连查
        FeaturedResourceVO featuredResourceVO = featuredResourceMapper.selectById(ResId);
        if (featuredResourceVO == null) {
            throw new RuntimeException("主推记录不存在");
        }
        // 查询 uploaderName（需要传 resource_id，不是主推ID）
        String uploaderName = resourceMapper.selectNameByUserId(featuredResourceVO.getResourceId());
        featuredResourceVO.setUploaderName(uploaderName);
        return featuredResourceVO;
    }
}
