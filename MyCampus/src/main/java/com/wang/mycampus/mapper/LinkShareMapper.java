package com.wang.mycampus.mapper;

import com.wang.mycampus.pojo.LinkShare;
import com.wang.mycampus.vo.LinkShareVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface LinkShareMapper {

    /*
    * 分页查询 ---> 关键词 + 来源名称
    * */
    List<LinkShareVO> selectList(@Param("keyword") String keyword, @Param("sourceName") String sourceName);

    /*
    * 根据 id 查询（内部用：管理员增删改查，不限制 status）
    * */
    LinkShareVO selectById(@Param("id") Long id);

    /*
    * 根据 id 查询（公开用：前台展示，只返回 status = 1）
    * */
    LinkShareVO selectByIdPublic(@Param("id") Long id);


    void insert(@Param("linkShare") LinkShare linkShare);

    void updateById(@Param("linkShare") LinkShare linkShare);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    void deleteById(@Param("id") Long id);

    List<LinkShareVO> selectNoBanList(String keyword, String sourceName);
}
