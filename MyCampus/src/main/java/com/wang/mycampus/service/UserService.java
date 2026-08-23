package com.wang.mycampus.service;


import com.wang.mycampus.dto.LoginDTO;
import com.wang.mycampus.dto.RegisterDTO;
import com.wang.mycampus.dto.UpdatePasswordDTO;
import com.wang.mycampus.dto.UpdateUserDTO;
import com.wang.mycampus.vo.LoginVO;
import com.wang.mycampus.vo.UserInfoVO;
import org.springframework.web.multipart.MultipartFile;


public interface UserService {
    // 注册一个新用户
    void insertUser(RegisterDTO registerDTO);
    // 用户登录
    LoginVO selectUser(LoginDTO loginDTO);

    // 更改用户的基本信息
    void updateUserInfoByUserId(Long userId, UpdateUserDTO updateUserDTO);

    // 判断是否是原密码 ---> 是 ---> 进行修改
    void isOrNotUserPassword(UpdatePasswordDTO updatePasswordDTO);

    // 根据 id 获取用户所用信息
    UserInfoVO selectUserById(Long id);

    // 上传用户头像
    String uploadAvatar(Long userId, MultipartFile file);
}
