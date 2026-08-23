package com.wang.mycampus.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.wang.mycampus.Utils.UserContext;
import com.wang.mycampus.annotation.RequireLogin;
import com.wang.mycampus.config.OssConfig;
import com.wang.mycampus.mapper.ConvertRecordMapper;
import com.wang.mycampus.pojo.ConvertRecord;
import com.wang.mycampus.service.ToolboxService;
import com.wang.mycampus.vo.ConvertResultVO;
import com.wang.mycampus.vo.ConvertHistoryVO;
import com.wang.mycampus.vo.PageVO;
import com.wang.mycampus.vo.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("")
public class ToolboxController {

    @Autowired
    private ToolboxService toolboxService;

    @Autowired
    private ConvertRecordMapper convertRecordMapper;

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssConfig ossConfig;

    /*
     * 1. 文件转换
     * 无需登录也可使用
     * */
    @PostMapping("/api/toolbox/convert")
    public Result<ConvertResultVO> convertFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetType") String targetType,
            @RequestParam(value = "quality", defaultValue = "90") Integer quality,
            @RequestParam(value = "width", required = false) Integer width,
            @RequestParam(value = "height", required = false) Integer height) {

        // 获取当前用户ID（可能为 null —— 未登录用户也可以转换）
        Long userId = UserContext.getUserId();

        log.info("文件转换: name={}, size={}, targetType={}, userId={}",
                file.getOriginalFilename(), file.getSize(), targetType, userId);

        try {
            ConvertResultVO result = toolboxService.convertFile(file, targetType, quality, width, height, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("文件转换异常", e);
            return Result.error(500, e.getMessage());
        }
    }

    /*
     * 2. 获取转换记录列表（登录后才可查看自己的记录）
     * */
    @RequireLogin
    @GetMapping("/api/toolbox/convert/history")
    public Result<PageVO<ConvertHistoryVO>> getConvertHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        log.info("获取转换记录列表 userId={}, page={}, size={}", userId, page, size);
        PageVO<ConvertHistoryVO> history = toolboxService.getConvertHistory(userId, page, size);
        return Result.success(history);
    }

    /*
     * 3. 获取支持的转换格式列表
     * */
    @GetMapping("/api/toolbox/convert/supported-types")
    public Result<Map<String, Object>> getSupportedTypes() {
        log.info("获取支持的转换格式列表");
        Map<String, Object> types = toolboxService.getSupportedTypes();
        return Result.success(types);
    }

    /*
     * 4. 下载转换结果（从 OSS 代理流到浏览器，不要求 bucket 公开）
     * */
    @GetMapping("/api/toolbox/download/{id}")
    public void downloadResult(@PathVariable Long id, HttpServletResponse response) {
        ConvertRecord record = convertRecordMapper.selectById(id);
        if (record == null || record.getResultUrl() == null) {
            response.setStatus(404);
            return;
        }

        log.info("下载转换结果 id={}, source={}→{}", id, record.getSourceType(), record.getTargetType());

        try {
            OSSObject ossObject = ossClient.getObject(ossConfig.getBucketName(), record.getResultUrl());

            // 设置响应头（用 OSS 实际 key 的扩展名，兼容 ZIP 等多页场景）
            String ossKey = record.getResultUrl();
            String actualExt = ossKey.contains(".") ? ossKey.substring(ossKey.lastIndexOf(".") + 1) : record.getTargetType();
            String fileName = record.getSourceName().replaceFirst("\\.[^.]+$", "")
                    + "." + actualExt;
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 流式输出
            try (InputStream is = ossObject.getObjectContent();
                 OutputStream os = response.getOutputStream()) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = is.read(buf)) != -1) {
                    os.write(buf, 0, len);
                }
                os.flush();
            }
        } catch (Exception e) {
            log.error("下载转换结果失败 id={}", id, e);
            response.setStatus(500);
        }
    }

    /*
     * 5. 删除转换记录
     * */
    @RequireLogin
    @DeleteMapping("/api/toolbox/convert/{id}")
    public Result<Void> deleteConvertRecord(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        log.info("删除转换记录 id={}, userId={}", id, userId);
        toolboxService.deleteConvertRecord(id, userId);
        return Result.success();
    }
}