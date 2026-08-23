package com.wang.mycampus.service;

import com.wang.mycampus.dto.FeaturedResourceAddDTO;
import com.wang.mycampus.dto.FeaturedResourceQueryDTO;
import com.wang.mycampus.dto.FeaturedResourceUpdateDTO;
import com.wang.mycampus.vo.FeaturedResourceVO;
import com.wang.mycampus.vo.PageVO;

public interface FeaturedResourceService {

    PageVO<FeaturedResourceVO> selectList(FeaturedResourceQueryDTO featuredResourceQueryDTO);

    PageVO<FeaturedResourceVO> getAdminFeaturedList(FeaturedResourceQueryDTO queryDTO);

    void insertOne(Long userId, FeaturedResourceAddDTO featuredResourceAddDTO);


    void update(Long id, FeaturedResourceUpdateDTO featuredResourceUpdateDTO);

    void deleteById(Long id);

    FeaturedResourceVO getDetail(Long id);
}
