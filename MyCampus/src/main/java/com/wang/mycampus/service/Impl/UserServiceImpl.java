package com.wang.mycampus.service.Impl;

import com.aliyun.oss.OSS;
import com.wang.mycampus.Utils.JWT;
import com.wang.mycampus.Utils.UserContext;
import com.wang.mycampus.config.OssConfig;
import com.wang.mycampus.dto.LoginDTO;
import com.wang.mycampus.dto.RegisterDTO;
import com.wang.mycampus.dto.UpdatePasswordDTO;
import com.wang.mycampus.dto.UpdateUserDTO;
import com.wang.mycampus.exception.BaseException;
import com.wang.mycampus.exception.LoginException;
import com.wang.mycampus.exception.PasswordException;
import com.wang.mycampus.mapper.UserMapper;
import com.wang.mycampus.pojo.User;
import com.wang.mycampus.service.UserService;
import com.wang.mycampus.vo.LoginVO;
import com.wang.mycampus.vo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssConfig ossConfig;

    /*
     * 新注册一个用户
     * */
    @Override
    public void insertUser(RegisterDTO registerDTO) {
        // 先检查用户名是否已存在
        User existUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new LoginException(400, "用户名已存在~");
        }
        userMapper.insertOneUser(registerDTO);
    }

    /*
     * 用户登录
     * */
    @Override
    public LoginVO selectUser(LoginDTO loginDTO) {
        // 先进行查询，若查到了，给一个token
        LoginVO loginVO = userMapper.selectUser(loginDTO);
        // 若没查到 ---> 抛出异常?
        // throw new LoginException("登录异常!请先进行登录~");
        if (loginVO == null) {
            throw new LoginException(404, "用户不存在~");
        }

        // 获取 用户 id username
        String username = loginVO.getUsername();
        Long userId = loginVO.getId();
        // 给 token
        String jwt = JWT.generateToken(userId, username);
        loginVO.setToken(jwt);
        return loginVO;

    }

    /*
    *更改用户的基本信息
    * */
    @Override
    public void updateUserInfoByUserId(Long userId, UpdateUserDTO updateUserDTO) {
        // 直接进进行用户的更改
        userMapper.updateUserBaseInfo(userId,updateUserDTO);
    }

    /*
    * 判断是否是原密码
    * 是 ---> 修改 update
    * 否 ---> 报错并提示
    * 不要忘记加事务管理 ---> 有多个sql语句
    * */
    @Transactional
    @Override
    public void isOrNotUserPassword(UpdatePasswordDTO updatePasswordDTO) {
        // 从 ThreadLocal 获取当前登录用户 ID
        Long userId = UserContext.getUserId();
        String oldPassword = updatePasswordDTO.getOldPassword();
        String newPassword = updatePasswordDTO.getNewPassword();

        // 根据 userId + 旧密码查询，确保只能修改自己的密码
        if (userMapper.selectByUserIdAndPassword(userId, oldPassword) != null) {
            userMapper.updateUserPassword(userId, oldPassword, newPassword);
            return;
        }

        // 否则 ---> 报错
        throw new PasswordException(401, "原密码错误~");
    }

    @Override
    public UserInfoVO selectUserById(Long id) {
        return userMapper.selectById(id);
    }

    /*
    * 上传用户头像
    * 1. 校验文件是否为空
    * 2. 校验文件类型（仅允许 jpg/jpeg/png/webp）
    * 3. 校验文件大小（最大 5MB）
    * 4. 上传到 OSS
    * 5. 更新用户 avatar 字段
    * 6. 返回头像 URL
    * */
    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 1. 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BaseException(400, "请选择要上传的头像图片");
        }

        // 2. 校验文件类型
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        String fileType = ext.replace(".", "");
        if (!List.of("jpg", "jpeg", "png", "webp").contains(fileType)) {
            throw new BaseException(400, "仅支持 jpg/png/webp 格式的头像图片");
        }

        // 3. 校验文件大小（最大 5MB）
        long fileSize = file.getSize();
        if (fileSize > 5 * 1024 * 1024) {
            throw new BaseException(400, "头像图片大小不能超过 5MB");
        }

        // 4. 生成 OSS 存储路径：avatar/user_{userId}_{uuid}.{ext}
        String objectName = "avatar/user_" + userId + "_" + UUID.randomUUID().toString().replace("-", "") + ext;

        // 5. 上传到 OSS
        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);
        } catch (Exception e) {
            log.error("OSS 头像上传失败", e);
            throw new BaseException(500, "头像上传失败，请稍后重试");
        }

        // 6. 构造头像 URL：https://{bucketName}.{endpoint-host}/{objectName}
        String endpoint = ossConfig.getEndpoint();  // https://oss-cn-beijing.aliyuncs.com
        String host = endpoint.replace("https://", "").replace("http://", "");
        String avatarUrl = "https://" + ossConfig.getBucketName() + "." + host + "/" + objectName;

        // 7. 更新用户头像字段
        userMapper.updateAvatar(userId, avatarUrl);

        log.info("用户 {} 头像上传成功: {}", userId, avatarUrl);
        return avatarUrl;
    }
}
