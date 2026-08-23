package com.wang.mycampus.service.Impl;

import com.aliyun.oss.OSS;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wang.mycampus.config.OssConfig;
import com.wang.mycampus.exception.BaseException;
import com.wang.mycampus.mapper.ResourceMapper;
import com.wang.mycampus.mapper.ResourceUploadBatchMapper;
import com.wang.mycampus.pojo.Resource;
import com.wang.mycampus.pojo.ResourceUploadBatch;
import com.wang.mycampus.service.ResourceService;
import com.wang.mycampus.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private ResourceUploadBatchMapper batchMapper;

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssConfig ossConfig;

    /** 批次号生成锁，保证同 JVM 内线程安全 */
    private final Object batchNoLock = new Object();

    /*
     * 上传资料到 OSS
     * */
    @Override
    public ResourceUploadVO uploadResource(Long userId, MultipartFile file, String title, String description) throws IOException {
        // 校验文件是否为空
        if (file.isEmpty()) {
            throw new BaseException(400, "请选择要上传的文件");
        }

        // 获取原始文件名和扩展名
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        // 校验文件类型
        String fileType = ext.replace(".", "");
        if (!List.of("pdf", "doc", "docx").contains(fileType)) {
            throw new BaseException(400, "仅支持 pdf/doc/docx 格式文件");
        }

        // 校验文件大小（最大 50MB）
        long fileSize = file.getSize();
        if (fileSize > 50 * 1024 * 1024) {
            throw new BaseException(400, "文件大小不能超过 50MB");
        }

        // 生成 OSS 存储路径：resources/{userId}/{uuid}.{ext}
        String objectName = "resources/" + userId + "/" + UUID.randomUUID() + ext;

        // 上传到 OSS
        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);
        } catch (Exception e) {
            log.error("OSS 上传失败", e);
            throw new BaseException(500, "文件上传失败，请稍后重试");
        }

        // 保存资料记录到数据库
        Resource resource = new Resource();
        resource.setUserId(userId);
        resource.setTitle(title);
        resource.setDescription(description);
        resource.setFileUrl(objectName);
        resource.setFileType(fileType);
        resource.setFileSize(fileSize);
        resource.setDownloadCount(0);
        resource.setUploadType("single");
        resource.setCreateTime(LocalDateTime.now());

        resourceMapper.insertResource(resource);

        // 组装响应
        ResourceUploadVO vo = new ResourceUploadVO();
        vo.setId(resource.getId());
        vo.setTitle(resource.getTitle());
        vo.setFileType(resource.getFileType());
        vo.setFileSize(resource.getFileSize());
        vo.setCreateTime(resource.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return vo;
    }

    /*
     * ==============================
     * 文件夹上传（新增）
     * ==============================
     */
    @Override
    public ResourceFolderUploadResultVO uploadFolder(Long userId, String folderName, String description,
                                                      MultipartFile[] files, String[] relativePaths) throws IOException {
        // ========== Phase 1：参数校验 ==========
        // folderName 校验
        if (folderName == null || folderName.trim().isEmpty()) {
            throw new BaseException(400, "文件夹名称不能为空");
        }
        folderName = folderName.trim();

        // files 校验
        if (files == null || files.length == 0) {
            throw new BaseException(400, "请选择要上传的文件夹");
        }

        // relativePaths 校验
        if (relativePaths == null || relativePaths.length == 0) {
            throw new BaseException(400, "文件相对路径不能为空");
        }

        // 数量校验
        if (files.length != relativePaths.length) {
            throw new BaseException(400, "文件数量与路径数量不一致");
        }

        // 文件数上限校验
        if (files.length > 100) {
            throw new BaseException(400, "文件夹中文件数量不能超过100个");
        }

        // 计算总大小 & 校验总大小
        long totalSize = 0L;
        for (MultipartFile file : files) {
            totalSize += file.getSize();
        }
        if (totalSize > 500L * 1024 * 1024) {
            throw new BaseException(400, "整个文件夹总大小不能超过500MB");
        }

        // 逐文件基础校验 & relativePath 安全校验
        for (int i = 0; i < files.length; i++) {
            validateSingleFileBeforeUpload(files[i], i);
            validateRelativePath(relativePaths[i], i);
        }

        // ========== Phase 2：生成批次号 ==========
        String batchNo = generateBatchNo();

        // ========== Phase 3：写入批次记录（初始状态处理中） ==========
        ResourceUploadBatch batch = new ResourceUploadBatch();
        batch.setBatchNo(batchNo);
        batch.setUserId(userId);
        batch.setFolderName(folderName);
        batch.setDescription(description != null && !description.trim().isEmpty() ? description.trim() : null);
        batch.setTotalCount(files.length);
        batch.setSuccessCount(0);
        batch.setFailCount(0);
        batch.setTotalSize(totalSize);
        batch.setStatus(0); // 处理中
        batchMapper.insertBatch(batch);

        // ========== Phase 4：逐文件上传到 OSS 并入库 ==========
        List<ResourceFolderItemVO> successRecords = new ArrayList<>();
        List<FailedUploadFileVO> failedFiles = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String relativePath = relativePaths[i];
            String originalFilename = file.getOriginalFilename();

            try {
                // 获取扩展名
                String ext = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
                }
                String fileType = ext.replace(".", "");

                // 二次校验文件类型（catch 中记录失败，不抛异常）
                if (!List.of("pdf", "doc", "docx").contains(fileType)) {
                    throw new BaseException(400, "仅支持上传pdf/doc/docx文件");
                }

                // 二次校验单文件大小
                if (file.getSize() > 50L * 1024 * 1024) {
                    throw new BaseException(400, "单个文件大小不能超过50MB");
                }

                // 生成 OSS 存储路径（安全：后端自行生成，不使用 relativePath）
                // 格式：resources/{yyyyMMdd}/{userId}/{batchNo}/{uuid}-{原始文件名}
                String objectName = "resources/" + dateStr + "/" + userId + "/" + batchNo + "/"
                        + UUID.randomUUID().toString().replace("-", "") + "-" + originalFilename;

                // 上传到 OSS
                try (InputStream inputStream = file.getInputStream()) {
                    ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);
                } catch (Exception e) {
                    log.error("OSS 上传失败: {}", originalFilename, e);
                    throw new BaseException(500, "文件上传失败，请稍后重试");
                }

                // 标题 = 文件名去掉扩展名
                String title = originalFilename;
                if (originalFilename != null && originalFilename.contains(".")) {
                    title = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                }

                // 写入 resource 表
                Resource resource = new Resource();
                resource.setUserId(userId);
                resource.setTitle(title);
                resource.setDescription(description);
                resource.setFileUrl(objectName);
                resource.setFileType(fileType);
                resource.setFileSize(file.getSize());
                resource.setDownloadCount(0);
                resource.setUploadType("folder");
                resource.setBatchNo(batchNo);
                resource.setFolderName(folderName);
                resource.setRelativePath(relativePath);
                resource.setCreateTime(LocalDateTime.now());

                resourceMapper.insertResource(resource);

                // 组装成功 VO
                ResourceFolderItemVO itemVO = new ResourceFolderItemVO();
                itemVO.setId(resource.getId());
                itemVO.setTitle(title);
                itemVO.setFileType(fileType);
                itemVO.setFileSize(file.getSize());
                itemVO.setFolderName(folderName);
                itemVO.setRelativePath(relativePath);
                itemVO.setBatchNo(batchNo);
                itemVO.setCreateTime(resource.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                successRecords.add(itemVO);
                successCount++;

            } catch (Exception e) {
                // 记录失败，继续处理下一个文件
                log.warn("文件上传失败: {} ({}), 原因: {}", originalFilename, relativePath, e.getMessage());

                FailedUploadFileVO failedVO = new FailedUploadFileVO();
                failedVO.setFileName(originalFilename);
                failedVO.setRelativePath(relativePath);
                failedVO.setReason(e.getMessage());
                failedFiles.add(failedVO);
                failCount++;
            }
        }

        // ========== Phase 5：判定最终批次状态 ==========
        int finalStatus;
        if (failCount == files.length) {
            finalStatus = 3; // 全部失败
        } else if (failCount == 0) {
            finalStatus = 1; // 全部成功
        } else {
            finalStatus = 2; // 部分成功
        }

        // ========== Phase 6：更新批次最终结果 ==========
        batchMapper.updateBatchResult(batchNo, successCount, failCount, finalStatus);

        // ========== Phase 7：组装返回 VO ==========
        ResourceFolderUploadResultVO resultVO = new ResourceFolderUploadResultVO();
        resultVO.setBatchNo(batchNo);
        resultVO.setFolderName(folderName);
        resultVO.setTotalCount(files.length);
        resultVO.setSuccessCount(successCount);
        resultVO.setFailCount(failCount);
        resultVO.setTotalSize(totalSize);
        resultVO.setStatus(finalStatus);
        resultVO.setRecords(successRecords);
        resultVO.setFailedFiles(failedFiles);

        return resultVO;
    }

    // ========== 文件夹上传辅助方法 ==========

    /**
     * 生成批次号：RB + yyyyMMdd + 4位序号（线程安全）
     */
    private String generateBatchNo() {
        synchronized (batchNoLock) {
            String todayPrefix = "RB" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String maxBatchNo = batchMapper.selectMaxBatchNoForToday(todayPrefix);

            int nextSeq;
            if (maxBatchNo == null || maxBatchNo.isEmpty()) {
                nextSeq = 1;
            } else {
                String seqStr = maxBatchNo.substring(todayPrefix.length());
                nextSeq = Integer.parseInt(seqStr) + 1;
            }

            if (nextSeq > 9999) {
                throw new BaseException(500, "今日上传批次已达上限(9999)，请明日再试");
            }

            return todayPrefix + String.format("%04d", nextSeq);
        }
    }

    /**
     * 单文件基础校验（上传前统一校验，失败抛异常）
     */
    private void validateSingleFileBeforeUpload(MultipartFile file, int index) {
        String originalFilename = file.getOriginalFilename();

        if (file.isEmpty()) {
            throw new BaseException(400, "第" + (index + 1) + "个文件为空");
        }

        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        String fileType = ext.replace(".", "");

        if (!List.of("pdf", "doc", "docx").contains(fileType)) {
            throw new BaseException(400, "仅支持上传pdf/doc/docx文件（第" + (index + 1) + "个文件：" + originalFilename + "）");
        }

        if (file.getSize() > 50L * 1024 * 1024) {
            throw new BaseException(400, "单个文件大小不能超过50MB（第" + (index + 1) + "个文件：" + originalFilename + "）");
        }
    }

    /**
     * relativePath 安全校验
     */
    private void validateRelativePath(String relativePath, int index) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new BaseException(400, "第" + (index + 1) + "个文件的relativePath不能为空");
        }

        String path = relativePath.trim();

        if (path.contains("..")) {
            throw new BaseException(400, "第" + (index + 1) + "个文件的relativePath包含非法字符'..'");
        }
        if (path.contains("\\")) {
            throw new BaseException(400, "第" + (index + 1) + "个文件的relativePath包含非法字符'\\'");
        }
        if (path.startsWith("/")) {
            throw new BaseException(400, "第" + (index + 1) + "个文件的relativePath不能以'/'开头");
        }
    }

    // ========== 文件夹上传辅助方法结束 ==========

    /*
     * 获取资料列表（分页）
     * */
    @Override
    public PageVO<ResourceItemVO> getResourceList(String fileType, String keyword, Integer page, Integer size) {
        Page<Object> objects = PageHelper.startPage(page, size);
        List<ResourceItemVO> resourceList = resourceMapper.selectResourceList(keyword, fileType);

        PageVO<ResourceItemVO> pageVO = new PageVO<>();
        pageVO.setRecords(resourceList);
        pageVO.setTotal((int) objects.getTotal());
        pageVO.setPage(page);
        pageVO.setSize(size);
        return pageVO;
    }

    /*
     * 获取"我的资源"列表（分页，按用户ID过滤）
     */
    @Override
    public PageVO<ResourceItemVO> getMyResourceList(Long userId, String fileType, String keyword,
                                                     String uploadType, Integer page, Integer size) {
        // 分页参数兜底
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        if (size > 50) size = 50; // 最大每页 50 条

        // 枚举字段校验
        if (fileType != null && !fileType.trim().isEmpty()
                && !List.of("pdf", "doc", "docx").contains(fileType.trim())) {
            throw new BaseException(400, "文件类型参数不合法");
        }
        if (uploadType != null && !uploadType.trim().isEmpty()
                && !List.of("single", "folder").contains(uploadType.trim())) {
            throw new BaseException(400, "上传类型参数不合法");
        }

        Page<Object> objects = PageHelper.startPage(page, size);
        List<ResourceItemVO> resourceList = resourceMapper.selectMyResourceList(
                userId, keyword, fileType, uploadType);

        PageVO<ResourceItemVO> pageVO = new PageVO<>();
        pageVO.setRecords(resourceList);
        pageVO.setTotal((int) objects.getTotal());
        pageVO.setPage(page);
        pageVO.setSize(size);
        return pageVO;
    }

    /*
     * 获取资料详情
     * */
    @Override
    public ResourceItemVO getResourceDetail(Long id) {
        Resource resource = resourceMapper.selectResourceById(id);
        if (resource == null) {
            throw new BaseException(404, "资料不存在");
        }

        String uploaderName = resourceMapper.selectNicknameByUserId(resource.getUserId());
        if (uploaderName == null) {
            uploaderName = "未知用户";
        }

        ResourceItemVO vo = new ResourceItemVO();
        vo.setId(resource.getId());
        vo.setTitle(resource.getTitle());
        vo.setDescription(resource.getDescription());
        vo.setFileType(resource.getFileType());
        vo.setFileSize(resource.getFileSize());
        vo.setDownloadCount(resource.getDownloadCount());
        vo.setUploaderName(uploaderName);
        vo.setUploadType(resource.getUploadType());
        vo.setFolderName(resource.getFolderName());
        vo.setRelativePath(resource.getRelativePath());
        vo.setBatchNo(resource.getBatchNo());
        vo.setCreateTime(resource.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return vo;
    }

    /*
     * 下载资料
     * */
    @Override
    public String downloadResource(Long id, Long userId) {
        Resource resource = resourceMapper.selectResourceById(id);
        if (resource == null) {
            throw new BaseException(404, "资料不存在");
        }

        // 登录用户才统计下载次数
        if (userId != null) {
            resourceMapper.incrementDownloadCount(id);
        }

        // 返回 OSS 文件的 objectName
        return resource.getFileUrl();
    }

    /*
     * 删除资料（仅上传者和管理员可删除）
     * */
    @Transactional
    @Override
    public void deleteResource(Long id, Long userId) {
        Resource resource = resourceMapper.selectResourceById(id);
        if (resource == null) {
            throw new BaseException(404, "资料不存在");
        }

        // 检查权限：上传者本人 或 管理员
        Integer role = resourceMapper.selectUserRoleByUserId(userId);
        boolean isAdmin = role != null && role == 1;
        boolean isUploader = resource.getUserId().equals(userId);

        if (!isAdmin && !isUploader) {
            throw new BaseException(403, "无权删除该资料");
        }

        // 从 OSS 删除文件
        try {
            ossClient.deleteObject(ossConfig.getBucketName(), resource.getFileUrl());
        } catch (Exception e) {
            log.error("OSS 删除文件失败: {}", resource.getFileUrl(), e);
            // 即使 OSS 删除失败，也继续删除数据库记录
        }

        // 删除数据库记录
        resourceMapper.deleteResourceById(id);
    }

    @Override
    public Integer selectResourceNumber() {
        return resourceMapper.selectNumber();
    }

    // 更新下载量数据
    @Override
    public void updateDownLoadNumber(Long id) {
        resourceMapper.updateLoadNumber(id);
    }
}
