package com.wang.mycampus.controller;

import com.wang.mycampus.annotation.RequireLogin;
import com.wang.mycampus.dto.AddMemoDTO;
import com.wang.mycampus.dto.UpdateMemoDTO;
import com.wang.mycampus.dto.UpdateMemoStatusDTO;
import com.wang.mycampus.dto.UpdateMemoPinDTO;
import com.wang.mycampus.service.MemoService;
import com.wang.mycampus.vo.MemoVO;
import com.wang.mycampus.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("")
public class MemoController {

    @Autowired
    private MemoService memoService;

    /*
     * 获取当前用户的备忘录列表
     */
    @RequireLogin
    @GetMapping("/api/memo/list")
    public Result<List<MemoVO>> getMemoList() {
        log.info("获取备忘录列表...");
        List<MemoVO> list = memoService.getMemoList();
        return Result.success(list);
    }

    /*
     * 新增备忘录
     */
    @RequireLogin
    @PostMapping("/api/memo/add")
    public Result<MemoVO> addMemo(@Valid @RequestBody AddMemoDTO addMemoDTO) {
        log.info("新增备忘录: {}", addMemoDTO);
        MemoVO memoVO = memoService.addMemo(addMemoDTO);
        return Result.success(memoVO);
    }

    /*
     * 修改备忘录内容
     */
    @RequireLogin
    @PutMapping("/api/memo/{id}")
    public Result<MemoVO> updateMemo(@PathVariable Long id,
                                     @Valid @RequestBody UpdateMemoDTO updateMemoDTO) {
        log.info("修改备忘录内容 id={}, body={}", id, updateMemoDTO);
        MemoVO memoVO = memoService.updateMemo(id, updateMemoDTO);
        return Result.success(memoVO);
    }

    /*
     * 修改完成状态
     */
    @RequireLogin
    @PutMapping("/api/memo/{id}/status")
    public Result<MemoVO> updateStatus(@PathVariable Long id,
                                       @Valid @RequestBody UpdateMemoStatusDTO dto) {
        log.info("修改备忘录状态 id={}, status={}", id, dto.getStatus());
        MemoVO memoVO = memoService.updateStatus(id, dto);
        return Result.success(memoVO);
    }

    /*
     * 修改置顶状态
     */
    @RequireLogin
    @PutMapping("/api/memo/{id}/pin")
    public Result<MemoVO> updatePinned(@PathVariable Long id,
                                       @Valid @RequestBody UpdateMemoPinDTO dto) {
        log.info("修改备忘录置顶 id={}, isPinned={}", id, dto.getIsPinned());
        MemoVO memoVO = memoService.updatePinned(id, dto);
        return Result.success(memoVO);
    }

    /*
     * 删除备忘录
     */
    @RequireLogin
    @DeleteMapping("/api/memo/{id}")
    public Result<Boolean> deleteMemo(@PathVariable Long id) {
        log.info("删除备忘录 id={}", id);
        memoService.deleteMemo(id);
        return Result.success(true);
    }
}
