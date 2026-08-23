package com.wang.mycampus.mapper;

import com.wang.mycampus.pojo.Resource;
import com.wang.mycampus.vo.FileTypeDistributionVO;
import com.wang.mycampus.vo.HotResourceVO;
import com.wang.mycampus.vo.ResourceItemVO;
import com.wang.mycampus.vo.TrendItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ResourceMapper {


    /*
     * 插入资料记录
     * */
    int insertResource(Resource resource);

    /*
     * 根据ID查询资料
     * */
    Resource selectResourceById(@Param("id") Long id);

    /*
     * 分页查询资料列表（支持文件类型和关键词筛选）
     * */
    List<ResourceItemVO> selectResourceList(@Param("keyword") String keyword,
                                            @Param("fileType") String fileType);

    /*
     * 分页查询"我的资源"列表（按用户ID过滤 + 关键字/文件类型/上传类型筛选）
     */
    List<ResourceItemVO> selectMyResourceList(@Param("userId") Long userId,
                                              @Param("keyword") String keyword,
                                              @Param("fileType") String fileType,
                                              @Param("uploadType") String uploadType);

    /*
     * 删除资料
     * */
    int deleteResourceById(@Param("id") Long id);

    /*
     * 下载次数 +1
     * */
    int incrementDownloadCount(@Param("id") Long id);

    /*
     * 查询资料的上传者ID
     * */
    @Select("SELECT user_id FROM resource WHERE id = #{id}")
    Long selectUserIdByResourceId(@Param("id") Long id);

    /*
     * 查询用户的角色
     * */
    @Select("SELECT role FROM user WHERE id = #{userId}")
    Integer selectUserRoleByUserId(@Param("userId") Long userId);

    /*
     * 查询用户的昵称
     * */
    @Select("SELECT nickname FROM user WHERE id = #{userId}")
    String selectNicknameByUserId(@Param("userId") Long userId);

    /*
    * 查询所有的资料的数量
    * */
    @Select("select count(1) from resource")
    Integer selectNumber();

    /*
    * 更新下载量数据
    * */
    @Update("update resource set download_count = download_count + 1 where id = #{id}")
    void updateLoadNumber(@Param("id") Long id);

    /*
     * 查询下载总次数
     * */
    @Select("SELECT IFNULL(SUM(download_count), 0) FROM resource")
    Long selectTotalDownloadCount();

    /*
     * 资料上传趋势（近 N 天，按天分组）
     * */
    List<TrendItemVO> selectResourceUploadTrend(@Param("days") Integer days);

    /*
     * 文件类型分布
     * */
    List<FileTypeDistributionVO> selectFileTypeDistribution();

    /*
     * 热门资料 Top N
     * */
    List<HotResourceVO> selectDownloadTop(@Param("limit") Integer limit);

    /*
    * 两表联查
    * */
    String selectNameByUserId(@Param("resId") Long resId);
}
