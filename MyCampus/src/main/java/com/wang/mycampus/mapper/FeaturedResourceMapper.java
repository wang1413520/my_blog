package com.wang.mycampus.mapper;

import com.wang.mycampus.dto.FeaturedResourceAddDTO;
import com.wang.mycampus.dto.FeaturedResourceUpdateDTO;
import com.wang.mycampus.pojo.FeaturedResource;
import com.wang.mycampus.vo.FeaturedResourceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FeaturedResourceMapper {

    /*
    * 批量查询（前台公开）
    * */
    List<FeaturedResourceVO> selectList(@Param("keyword") String keyword);

    /*
    * 后台查询主推列表（管理用）
    * */
    List<FeaturedResourceVO> selectAdminList(@Param("keyword") String keyword, @Param("status") Integer status);

    /*
    * 根据 id 查询
    * */

    FeaturedResource selectByResourceId(@Param("resourceId")Long resourceId);

    /*
    * 插入一条新的主推
    * */
    void insertOne(@Param("userId") Long userId, @Param("featuredResourceAddDTO") FeaturedResourceAddDTO featuredResourceAddDTO);


    /*
    * 更新主推资源
    * */
    void updateById(@Param("id") Long id,@Param("featuredResourceUpdateDTO") FeaturedResourceUpdateDTO featuredResourceUpdateDTO);

    /*
    * 删除
    * */
    void deleteById(@Param("id") Long id);

    /*
    * 两表连查
    * */
    FeaturedResourceVO selectById(@Param("resId") Long resId);
}
