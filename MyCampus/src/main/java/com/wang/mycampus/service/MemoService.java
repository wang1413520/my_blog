package com.wang.mycampus.service;

import com.wang.mycampus.dto.AddMemoDTO;
import com.wang.mycampus.dto.UpdateMemoDTO;
import com.wang.mycampus.dto.UpdateMemoStatusDTO;
import com.wang.mycampus.dto.UpdateMemoPinDTO;
import com.wang.mycampus.vo.MemoVO;

import java.util.List;

public interface MemoService {

    /*
     * 获取当前用户的备忘录列表
     */
    List<MemoVO> getMemoList();

    /*
     * 新增备忘录
     */
    MemoVO addMemo(AddMemoDTO addMemoDTO);

    /*
     * 修改备忘录内容
     */
    MemoVO updateMemo(Long id, UpdateMemoDTO updateMemoDTO);

    /*
     * 修改完成状态
     */
    MemoVO updateStatus(Long id, UpdateMemoStatusDTO updateMemoStatusDTO);

    /*
     * 修改置顶状态
     */
    MemoVO updatePinned(Long id, UpdateMemoPinDTO updateMemoPinDTO);

    /*
     * 删除备忘录
     */
    void deleteMemo(Long id);
}
