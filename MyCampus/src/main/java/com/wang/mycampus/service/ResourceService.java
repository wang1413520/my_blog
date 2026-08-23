package com.wang.mycampus.service;

import com.wang.mycampus.vo.PageVO;
import com.wang.mycampus.vo.ResourceFolderUploadResultVO;
import com.wang.mycampus.vo.ResourceItemVO;
import com.wang.mycampus.vo.ResourceUploadVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ResourceService {

    /*
     * 上传资料
     * */
    ResourceUploadVO uploadResource(Long userId, MultipartFile file, String title, String description) throws IOException;

    /*
     * 文件夹上传
     * */
    ResourceFolderUploadResultVO uploadFolder(Long userId, String folderName, String description,
                                               MultipartFile[] files, String[] relativePaths) throws IOException;

    /*
     * 获取资料列表（分页）
     * */
    PageVO<ResourceItemVO> getResourceList(String fileType, String keyword, Integer page, Integer size);

    /*
     * 获取"我的资源"列表（分页，按用户ID过滤）
     * */
    PageVO<ResourceItemVO> getMyResourceList(Long userId, String fileType, String keyword,
                                             String uploadType, Integer page, Integer size);

    /*
     * 获取资料详情
     * */
    ResourceItemVO getResourceDetail(Long id);

    /*
     * 下载资料（返回 OSS 文件路径）
     * */
    String downloadResource(Long id, Long userId);

    /*
     * 删除资料
     * */
    void deleteResource(Long id, Long userId);

    /*
    * 查询所有的资料的数量
    * */
    Integer selectResourceNumber();


    /*
    * 下载量更新
    * */
    void updateDownLoadNumber(Long id);
}
