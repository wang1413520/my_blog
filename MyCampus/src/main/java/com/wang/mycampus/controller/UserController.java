package com.wang.mycampus.controller;


import com.wang.mycampus.Utils.UserContext;
import com.wang.mycampus.annotation.RequireLogin;
import com.wang.mycampus.dto.LoginDTO;
import com.wang.mycampus.dto.RegisterDTO;
import com.wang.mycampus.dto.UpdatePasswordDTO;
import com.wang.mycampus.dto.UpdateUserDTO;
import com.wang.mycampus.dto.UserProfileDetailUpdateDTO;
import com.wang.mycampus.service.UserProfileDetailService;
import com.wang.mycampus.service.UserService;
import com.wang.mycampus.vo.LoginVO;
import com.wang.mycampus.vo.Result;
import com.wang.mycampus.vo.UserInfoVO;
import com.wang.mycampus.vo.UserProfileDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController()
@RequestMapping("")
public class UserController {


    // 依赖注入
    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileDetailService userProfileDetailService;

    /*
     * 用户注册
     * */
    @PostMapping("/api/user/register")
    public Result registerUser(@RequestBody RegisterDTO registerDTO) {
        log.info("正在注册一个用户.... 具体信息为: {} ", registerDTO);
        userService.insertUser(registerDTO);
        return Result.success();
    }

    /*
     * 用户登录
     * */
    @PostMapping("/api/user/login")
    public Result<LoginVO> loginUser(@RequestBody LoginDTO loginDTO) {
        log.info("用户登录中... 登录用户的信息为: {}", loginDTO);
        LoginVO loginVO = userService.selectUser(loginDTO);
        return Result.success(loginVO);
    }

    /*
     * 修改当前用户的昵称和头像
     * */
    @RequireLogin
    @PutMapping("/api/user/update")
    public Result updateUserBaseInfo(@RequestBody UpdateUserDTO updateUserDTO) {
        log.info("更改用户的头像以及用户名,更改为: {} ", updateUserDTO);
        // 从 ThreadLocal 中获取当前用户 ID
        Long userId = UserContext.getUserId();
        userService.updateUserInfoByUserId(userId, updateUserDTO);
        return Result.success();
    }

    /*
     * 修改用户密码
     * */
    @RequireLogin
    @PutMapping("/api/user/password")
    public Result updateUserPassword(@RequestBody UpdatePasswordDTO updatePasswordDTO) {
        log.info("更改用户的密码: {} ", updatePasswordDTO);
        userService.isOrNotUserPassword(updatePasswordDTO);
        return Result.success();
    }


    /*
     *获取用户信息
     * */
    @RequireLogin
    @GetMapping("/api/user/info")
    public Result<UserInfoVO> selectUserInfo() {
        // 根据 token 获取用户的id
        Long id = UserContext.getUserId();
        log.info("查询用户信息中... 用户 id 为:{}", id);
        UserInfoVO userInfoVO = userService.selectUserById(id);
        return Result.success(userInfoVO);
    }


    /*
     * 查询"我的资料"详情
     * 首次未填写资料时也返回 code=200（含仅 userId 的空对象）
     */
    @RequireLogin
    @GetMapping("/api/user/profile/detail")
    public Result<UserProfileDetailVO> getProfileDetail() {
        Long userId = UserContext.getUserId();
        log.info("查询用户资料详情 userId={}", userId);
        UserProfileDetailVO vo = userProfileDetailService.getByUserId(userId);
        return Result.success(vo);
    }

    /*
     * 保存"我的资料"（首次插入，后续更新）
     */
    @RequireLogin
    @PutMapping("/api/user/profile/detail")
    public Result<Boolean> saveProfileDetail(@RequestBody UserProfileDetailUpdateDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("保存用户资料 userId={}, dto={}", userId, dto);
        userProfileDetailService.saveOrUpdate(userId, dto);
        return Result.success(true);
    }

    /*
    * 头像上传接口
    * */
    @RequireLogin
    @PostMapping("/api/user/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getUserId();
        log.info("用户 {} 正在上传头像, 文件名: {}", userId, file.getOriginalFilename());
        String avatarUrl = userService.uploadAvatar(userId, file);
        return Result.success(avatarUrl);
    }


}
