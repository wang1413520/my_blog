package com.wang.mycampus.service;

import com.wang.mycampus.dto.LinkShareAddDTO;
import com.wang.mycampus.dto.LinkShareQueryDTO;
import com.wang.mycampus.dto.LinkShareUpdateStatusDTO;
import com.wang.mycampus.vo.LinkShareVO;
import com.wang.mycampus.vo.PageVO;
import com.wang.mycampus.vo.Result;

public interface LinkShareService {

    PageVO<LinkShareVO> selectList(LinkShareQueryDTO queryDTO);


    LinkShareVO selectById(Long id);

    LinkShareVO selectByIdPublic(Long id);

    Result<LinkShareVO> insertOne(LinkShareAddDTO linkShareAddDTO);

    Result<LinkShareVO> update(Long id, LinkShareAddDTO linkShareAddDTO);

    Result<Void> updateStatus(Long id, LinkShareUpdateStatusDTO statusDTO);

    Result<Void> deleteById(Long id);

}
