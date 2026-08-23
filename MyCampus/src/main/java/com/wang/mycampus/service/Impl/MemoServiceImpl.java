package com.wang.mycampus.service.Impl;

import com.wang.mycampus.Utils.UserContext;
import com.wang.mycampus.annotation.RequireLogin;
import com.wang.mycampus.dto.AddMemoDTO;
import com.wang.mycampus.dto.UpdateMemoDTO;
import com.wang.mycampus.dto.UpdateMemoStatusDTO;
import com.wang.mycampus.dto.UpdateMemoPinDTO;
import com.wang.mycampus.exception.BaseException;
import com.wang.mycampus.mapper.MemoMapper;
import com.wang.mycampus.pojo.Memo;
import com.wang.mycampus.service.MemoService;
import com.wang.mycampus.vo.MemoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MemoServiceImpl implements MemoService {

    @Autowired
    private MemoMapper memoMapper;

    /*
     * 获取当前用户的备忘录列表
     * 排序：置顶优先 → 未完成优先 → 最近更新优先
     */
    @Override
    public List<MemoVO> getMemoList() {
        Long userId = UserContext.getUserId();
        List<Memo> memoList = memoMapper.selectListByUserId(userId);
        return memoList.stream()
                .map(this::toMemoVO)
                .collect(Collectors.toList());
    }

    /*
     * 新增备忘录
     * 默认值：status = 0（未完成），isPinned = 0（未置顶）
     */
    @Override
    public MemoVO addMemo(AddMemoDTO addMemoDTO) {
        Long userId = UserContext.getUserId();

        // 校验内容
        String content = addMemoDTO.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new BaseException(400, "备忘录内容不能为空");
        }
        if (content.trim().length() > 120) {
            throw new BaseException(400, "备忘录内容不能超过120个字符");
        }

        Memo memo = new Memo();
        memo.setUserId(userId);
        memo.setContent(content.trim());
        memo.setStatus(0);      // 默认未完成
        memo.setIsPinned(0);    // 默认未置顶

        memoMapper.insertMemo(memo);

        // 插入后 memo.id 已被 MyBatis 回填
        Memo saved = memoMapper.selectByIdAndUserId(memo.getId(), userId);
        return toMemoVO(saved);
    }

    /*
     * 修改备忘录内容（仅允许修改 content）
     */
    @Override
    public MemoVO updateMemo(Long id, UpdateMemoDTO updateMemoDTO) {
        Long userId = UserContext.getUserId();

        // 校验内容
        String content = updateMemoDTO.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new BaseException(400, "备忘录内容不能为空");
        }
        if (content.trim().length() > 120) {
            throw new BaseException(400, "备忘录内容不能超过120个字符");
        }

        int rows = memoMapper.updateContent(id, userId, content.trim());
        if (rows == 0) {
            throw new BaseException(404, "备忘录不存在");
        }

        Memo updated = memoMapper.selectByIdAndUserId(id, userId);
        return toMemoVO(updated);
    }

    /*
     * 修改完成状态（0-未完成，1-已完成）
     */
    @Override
    public MemoVO updateStatus(Long id, UpdateMemoStatusDTO dto) {
        Long userId = UserContext.getUserId();

        int rows = memoMapper.updateStatus(id, userId, dto.getStatus());
        if (rows == 0) {
            throw new BaseException(404, "备忘录不存在");
        }

        Memo updated = memoMapper.selectByIdAndUserId(id, userId);
        return toMemoVO(updated);
    }

    /*
     * 修改置顶状态（0-取消置顶，1-置顶）
     */
    @Override
    public MemoVO updatePinned(Long id, UpdateMemoPinDTO dto) {
        Long userId = UserContext.getUserId();

        int rows = memoMapper.updatePinned(id, userId, dto.getIsPinned());
        if (rows == 0) {
            throw new BaseException(404, "备忘录不存在");
        }

        Memo updated = memoMapper.selectByIdAndUserId(id, userId);
        return toMemoVO(updated);
    }

    /*
     * 删除备忘录
     */
    @Override
    public void deleteMemo(Long id) {
        Long userId = UserContext.getUserId();

        int rows = memoMapper.deleteByIdAndUserId(id, userId);
        if (rows == 0) {
            throw new BaseException(404, "备忘录不存在");
        }
    }

    // ==================== 私有方法 ====================

    /*
     * 将 Memo 实体转换为 MemoVO
     * 时间字段从 LocalDateTime 转为 13 位毫秒时间戳
     */
    private MemoVO toMemoVO(Memo memo) {
        MemoVO vo = new MemoVO();
        vo.setId(memo.getId());
        vo.setContent(memo.getContent());
        vo.setStatus(memo.getStatus());
        vo.setIsPinned(memo.getIsPinned());

        // LocalDateTime → 13 位毫秒时间戳
        if (memo.getCreateTime() != null) {
            vo.setCreateTime(memo.getCreateTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
        }
        if (memo.getUpdateTime() != null) {
            vo.setUpdateTime(memo.getUpdateTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
        }

        return vo;
    }
}
