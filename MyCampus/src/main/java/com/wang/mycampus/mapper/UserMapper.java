package com.wang.mycampus.mapper;

import com.wang.mycampus.dto.LoginDTO;
import com.wang.mycampus.dto.RegisterDTO;
import com.wang.mycampus.dto.UpdateUserDTO;
import com.wang.mycampus.pojo.User;
import com.wang.mycampus.vo.LoginVO;
import com.wang.mycampus.vo.TrendItemVO;
import com.wang.mycampus.vo.UserInfoVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    /*
    * 增加一个用户
    * */
    @Insert("insert into my_web_school_project.user(username, password, nickname) values (#{username},#{password},#{nickname})")
    void insertOneUser(RegisterDTO registerDTO);

    /*
     * 防止同户名重复
     * */
    @Select("select id, username, password, nickname, avatar, role, create_time from user where username = #{username} ")
    User selectByUsername(String username);

    /*
     * 查询用户角色（1=管理员 0=普通用户）
     * */
    @Select("SELECT role FROM user WHERE id = #{userId}")
    Integer selectRoleByUserId(@Param("userId") Long userId);

    /*
    * 在登录时查询用户
    * */
    LoginVO selectUser(LoginDTO loginDTO);


    /*
    * 修改用户的基本信息
    * */
    void updateUserBaseInfo(@Param("userId") Long userId, @Param("updateUserDTO") UpdateUserDTO updateUserDTO);

    /*
    * 根据 userId + 旧密码查询（校验是否是当前用户的密码）
    * */
    @Select("select password from user where id = #{userId} and password = #{oldPassword}")
    String selectByUserIdAndPassword(@Param("userId") Long userId, @Param("oldPassword") String oldPassword);

    /*
    * 更改新密码（指定用户）
    * */
    @Update("update user set password = #{newPassword} where id = #{userId} and password = #{oldPassword}")
    void updateUserPassword(@Param("userId") Long userId, @Param("oldPassword") String oldPassword, @Param("newPassword") String newPassword);

    /*
    * 根据 id 查询用户所用信息
    *
    * */
    @Select("select id, username, password, nickname, avatar, role, create_time from user where id = #{id}")
    UserInfoVO selectById(@Param("id") Long id);

    /*
    * 更新用户头像
    * */
    @Update("update user set avatar = #{avatar} where id = #{userId}")
    void updateAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);

    /*
     * 查询用户总数
     * */
    @Select("SELECT COUNT(*) FROM user")
    Long selectUserCount();

    /*
     * 用户注册趋势（近 N 天，按天分组）
     * */
    List<TrendItemVO> selectUserRegisterTrend(@Param("days") Integer days);
}
