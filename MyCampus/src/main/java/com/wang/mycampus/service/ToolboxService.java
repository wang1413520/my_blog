package com.wang.mycampus.service;

import com.wang.mycampus.vo.ConvertHistoryVO;
import com.wang.mycampus.vo.ConvertResultVO;
import com.wang.mycampus.vo.PageVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface ToolboxService {

    /**
     * 执行文件转换
     *
     * @param file       源文件
     * @param targetType 目标格式（pdf/docx/txt/png/jpg/md/html）
     * @param quality    图片质量（仅图片有效）
     * @param width      图片宽度（仅图片有效）
     * @param height     图片高度（仅图片有效）
     * @param userId     用户ID（可能为 null）
     * @return 转换结果
     */
    ConvertResultVO convertFile(MultipartFile file, String targetType,
                                Integer quality, Integer width, Integer height,
                                Long userId) throws IOException;

    /**
     * 获取用户的转换记录列表
     */
    PageVO<ConvertHistoryVO> getConvertHistory(Long userId, Integer page, Integer size);

    /**
     * 获取支持的转换格式列表
     */
    Map<String, Object> getSupportedTypes();

    /**
     * 删除转换记录
     */
    void deleteConvertRecord(Long id, Long userId);
}