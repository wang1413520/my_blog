package com.wang.mycampus.service;

import com.wang.mycampus.dto.UserProfileDetailUpdateDTO;
import com.wang.mycampus.vo.UserProfileDetailVO;

public interface UserProfileDetailService {

    /*
     * 根据用户ID查询资料详情
     * 如果用户尚未填写资料，返回空对象（非null），保证 code=200
     */
    UserProfileDetailVO getByUserId(Long userId);

    /*
     * 保存或更新用户资料（insert or update）
     */
    void saveOrUpdate(Long userId, UserProfileDetailUpdateDTO dto);
}
