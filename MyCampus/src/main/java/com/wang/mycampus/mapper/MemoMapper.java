package com.wang.mycampus.mapper;

import com.wang.mycampus.pojo.Memo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemoMapper {

    /*
     * 查询当前用户的备忘录列表
     * 排序：置顶优先 → 未完成优先 → 最近更新优先
     */
    List<Memo> selectListByUserId(@Param("userId") Long userId);

    /*
     * 根据ID和用户ID查询单条备忘录
     */
    Memo selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /*
     * 新增备忘录
     */
    int insertMemo(@Param("memo") Memo memo);

    /*
     * 修改备忘录内容
     */
    int updateContent(@Param("id") Long id, @Param("userId") Long userId, @Param("content") String content);

    /*
     * 修改完成状态
     */
    int updateStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("status") Integer status);

    /*
     * 修改置顶状态
     */
    int updatePinned(@Param("id") Long id, @Param("userId") Long userId, @Param("isPinned") Integer isPinned);

    /*
     * 删除备忘录
     */
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
