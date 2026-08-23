package com.wang.mycampus.mapper;

import com.wang.mycampus.pojo.UserProfileDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileDetailMapper {

    /*
     * 根据用户ID查询资料详情
     */
    UserProfileDetail selectByUserId(@Param("userId") Long userId);

    /*
     * 插入资料（首次保存）
     */
    int insert(@Param("entity") UserProfileDetail entity);

    /*
     * 根据用户ID更新资料（后续保存）
     */
    int updateByUserId(@Param("entity") UserProfileDetail entity);
}
