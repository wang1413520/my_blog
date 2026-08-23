package com.wang.mycampus.mapper;

import com.wang.mycampus.pojo.Notice;
import com.wang.mycampus.vo.NoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {

    /*
     * 前台：所有启用公告，按创建时间倒序（最新在前）
     * */
    List<NoticeVO> selectActive();

    /*
     * 后台：条件查询（keyword 标题模糊 / status 状态），按创建时间倒序
     * */
    List<NoticeVO> selectPage(@Param("keyword") String keyword,
                              @Param("status") Integer status);

    /*
     * 根据ID查询公告
     * */
    Notice selectById(@Param("id") Long id);

    /*
     * 新增公告（回填自增ID）
     * */
    int insert(Notice notice);

    /*
     * 更新公告（动态更新非空字段，强制刷新 update_time 版本号）
     * */
    int update(Notice notice);

    /*
     * 删除公告
     * */
    int deleteById(@Param("id") Long id);
}
