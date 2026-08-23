package com.wang.mycampus.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.wang.mycampus.Utils.JWT;
import com.wang.mycampus.Utils.UserContext;
import com.wang.mycampus.annotation.RequireLogin;
import com.wang.mycampus.config.OssConfig;
import com.wang.mycampus.service.ResourceService;
import com.wang.mycampus.vo.PageVO;
import com.wang.mycampus.vo.ResourceFolderUploadResultVO;
import com.wang.mycampus.vo.ResourceItemVO;
import com.wang.mycampus.vo.ResourceUploadVO;
import com.wang.mycampus.vo.Result;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssConfig ossConfig;

    /*
     * 5.1 上传资料
     * */
    @RequireLogin
    @PostMapping("/api/resource/upload")
    public Result<ResourceUploadVO> uploadResource(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("title") String title,
                                                   @RequestParam(value = "description", required = false) String description) throws IOException {
        log.info("上传资料 title={}, fileSize={}", title, file.getSize());
        Long userId = UserContext.getUserId();
        ResourceUploadVO vo = resourceService.uploadResource(userId, file, title, description);
        return Result.success(vo);
    }

    /*
     * 5.2 上传资料文件夹
     * */
    @RequireLogin
    @PostMapping("/api/resource/upload/folder")
    public Result<ResourceFolderUploadResultVO> uploadFolder(
            @RequestParam("folderName") String folderName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("relativePaths") List<String> relativePaths) throws IOException {

        log.info("文件夹上传 folderName={}, fileCount={}", folderName, files != null ? files.size() : 0);
        Long userId = UserContext.getUserId();

        // Controller 层基础校验
        if (folderName == null || folderName.trim().isEmpty()) {
            return Result.error(400, "文件夹名称不能为空");
        }
        if (files == null || files.isEmpty()) {
            return Result.error(400, "请选择要上传的文件夹");
        }
        if (relativePaths == null || relativePaths.isEmpty()) {
            return Result.error(400, "文件相对路径不能为空");
        }
        if (files.size() != relativePaths.size()) {
            return Result.error(400, "文件数量与路径数量不一致");
        }

        // 转换为数组传递给 Service
        MultipartFile[] fileArray = files.toArray(new MultipartFile[0]);
        String[] pathArray = relativePaths.toArray(new String[0]);

        ResourceFolderUploadResultVO vo = resourceService.uploadFolder(
                userId, folderName, description, fileArray, pathArray);

        // 根据批次状态决定 HTTP 响应
        Integer status = vo.getStatus();
        if (status != null && status == 3) {
            // 全部失败 → code=400
            return Result.error(400, "文件夹上传失败");
        } else if (status != null && status == 2) {
            // 部分成功 → code=200，但 message 区分
            Result<ResourceFolderUploadResultVO> result = Result.success(vo);
            result.setMessage("文件夹上传完成，部分文件失败");
            return result;
        } else {
            // 全部成功 → code=200
            Result<ResourceFolderUploadResultVO> result = Result.success(vo);
            result.setMessage("文件夹上传成功");
            return result;
        }
    }

    /*
     * 5.2 获取资料列表（分页）
     * */
    @GetMapping("/api/resource/list")
    public Result<PageVO<ResourceItemVO>> getResourceList(@RequestParam(required = false) String fileType,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size) {
        log.info("获取资料列表 fileType={}, keyword={}, page={}, size={}", fileType, keyword, page, size);
        PageVO<ResourceItemVO> pageVO = resourceService.getResourceList(fileType, keyword, page, size);
        return Result.success(pageVO);
    }

    /*
     * 5.2+ 获取"我的资源"列表（仅当前登录用户）
     */
    @RequireLogin
    @GetMapping("/api/resource/my/list")
    public Result<PageVO<ResourceItemVO>> getMyResourceList(@RequestParam(required = false) String fileType,
                                                            @RequestParam(required = false) String keyword,
                                                            @RequestParam(required = false) String uploadType,
                                                            @RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        log.info("获取我的资源列表 userId={}, fileType={}, keyword={}, uploadType={}, page={}, size={}",
                userId, fileType, keyword, uploadType, page, size);
        PageVO<ResourceItemVO> pageVO = resourceService.getMyResourceList(userId, fileType, keyword, uploadType, page, size);
        return Result.success(pageVO);
    }

    /*
     * 5.3 下载资料
     * */
    @GetMapping("/api/resource/download/{id}")
    public void downloadResource(@PathVariable Long id,
                                 @RequestHeader(value = "Authorization", required = false) String token,
                                 HttpServletResponse response) throws IOException {
        log.info("下载资料 id={}", id);

        // 尝试解析 token（登录后才统计下载次数）
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            try {
                Claims claims = JWT.parseToken(token);
                userId = Long.valueOf(claims.getSubject());
            } catch (Exception e) {
                // Token 无效，不统计下载次数
            }
        }

        // 获取 OSS 文件路径
        String objectName = resourceService.downloadResource(id, userId);

        // 从 OSS 获取文件流并写入响应
        OSSObject ossObject = ossClient.getObject(ossConfig.getBucketName(), objectName);
        String originalFilename = objectName.substring(objectName.lastIndexOf("/") + 1);

        // 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(originalFilename, StandardCharsets.UTF_8));

        // 流式写入
        try (InputStream is = ossObject.getObjectContent();
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        }

        // 下载成功后将下载量更新
        resourceService.updateDownLoadNumber(id);
    }

    /*
     * 5.4 删除资料
     * */
    @RequireLogin
    @DeleteMapping("/api/resource/{id}")
    public Result<Void> deleteResource(@PathVariable Long id) {
        log.info("删除资料 id={}", id);
        Long userId = UserContext.getUserId();
        resourceService.deleteResource(id, userId);
        return Result.success();
    }

    /*
     * 5.5 获取资料详情
     * */
    @GetMapping("/api/resource/{id}")
    public Result<ResourceItemVO> getResourceDetail(@PathVariable Long id) {
        log.info("获取资料详情 id={}", id);
        ResourceItemVO detail = resourceService.getResourceDetail(id);
        return Result.success(detail);
    }
}
