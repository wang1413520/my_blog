package com.wang.mycampus.mapper;

import com.wang.mycampus.pojo.ConvertRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConvertRecordMapper {

    /*
     * 插入转换记录
     * */
    int insertConvertRecord(ConvertRecord record);

    /*
     * 根据 ID 查询转换记录
     * */
    ConvertRecord selectById(@Param("id") Long id);

    /*
     * 查询用户的转换记录列表（按创建时间倒序）
     * */
    List<ConvertRecord> selectByUserId(@Param("userId") Long userId);

    /*
     * 更新转换状态（成功/失败）
     * */
    int updateStatus(ConvertRecord record);

    /*
     * 删除转换记录
     * */
    int deleteById(@Param("id") Long id);
}