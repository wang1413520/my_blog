package com.wang.mycampus.service.Impl;

import com.wang.mycampus.dto.UserProfileDetailUpdateDTO;
import com.wang.mycampus.exception.BaseException;
import com.wang.mycampus.mapper.UserProfileDetailMapper;
import com.wang.mycampus.pojo.UserProfileDetail;
import com.wang.mycampus.service.UserProfileDetailService;
import com.wang.mycampus.vo.UserProfileDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserProfileDetailServiceImpl implements UserProfileDetailService {

    @Autowired
    private UserProfileDetailMapper mapper;

    // 手机号正则（中国大陆）
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    // 日期格式
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 日期时间格式
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public UserProfileDetailVO getByUserId(Long userId) {
        UserProfileDetail entity = mapper.selectByUserId(userId);

        if (entity == null) {
            // 首次查询尚未填写资料 → 返回仅含 userId 的空对象，code 仍为 200
            UserProfileDetailVO empty = new UserProfileDetailVO();
            empty.setUserId(userId);
            return empty;
        }

        return toVO(entity);
    }

    @Override
    public void saveOrUpdate(Long userId, UserProfileDetailUpdateDTO dto) {
        // 1. 字段校验
        validate(dto);

        // 2. 构建实体对象
        UserProfileDetail entity = new UserProfileDetail();
        entity.setUserId(userId);
        entity.setRealName(trimToNull(dto.getRealName()));
        entity.setGender(trimToNull(dto.getGender()));
        entity.setPhone(trimToNull(dto.getPhone()));
        entity.setEmail(trimToNull(dto.getEmail()));
        entity.setSchool(trimToNull(dto.getSchool()));
        entity.setCollege(trimToNull(dto.getCollege()));
        entity.setMajor(trimToNull(dto.getMajor()));
        entity.setGrade(trimToNull(dto.getGrade()));
        entity.setLocation(trimToNull(dto.getLocation()));
        entity.setBio(trimToNull(dto.getBio()));

        // birthday 字符串转 LocalDate
        if (dto.getBirthday() != null && !dto.getBirthday().trim().isEmpty()) {
            entity.setBirthday(LocalDate.parse(dto.getBirthday().trim(), DATE_FORMATTER));
        }

        // 3. 查询是否已有记录，决定 insert 还是 update
        UserProfileDetail existing = mapper.selectByUserId(userId);
        if (existing == null) {
            mapper.insert(entity);
            log.info("用户 {} 首次保存资料", userId);
        } else {
            mapper.updateByUserId(entity);
            log.info("用户 {} 更新资料", userId);
        }
    }

    // ==================== 私有方法 ====================

    /*
     * 将实体转为 VO
     */
    private UserProfileDetailVO toVO(UserProfileDetail entity) {
        UserProfileDetailVO vo = new UserProfileDetailVO();
        vo.setUserId(entity.getUserId());
        vo.setRealName(entity.getRealName());
        vo.setGender(entity.getGender());

        // LocalDate → yyyy-MM-dd
        if (entity.getBirthday() != null) {
            vo.setBirthday(entity.getBirthday().format(DATE_FORMATTER));
        }

        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
        vo.setSchool(entity.getSchool());
        vo.setCollege(entity.getCollege());
        vo.setMajor(entity.getMajor());
        vo.setGrade(entity.getGrade());
        vo.setLocation(entity.getLocation());
        vo.setBio(entity.getBio());

        // LocalDateTime → yyyy-MM-dd HH:mm:ss
        if (entity.getCreatedAt() != null) {
            vo.setCreateTime(entity.getCreatedAt().format(DATETIME_FORMATTER));
        }
        if (entity.getUpdatedAt() != null) {
            vo.setUpdateTime(entity.getUpdatedAt().format(DATETIME_FORMATTER));
        }

        return vo;
    }

    /*
     * 字段合法性校验
     */
    private void validate(UserProfileDetailUpdateDTO dto) {
        // gender 仅允许 male / female / unknown / null / 空
        String gender = dto.getGender();
        if (gender != null && !gender.trim().isEmpty()) {
            gender = gender.trim();
            if (!gender.equals("male") && !gender.equals("female") && !gender.equals("unknown")) {
                throw new BaseException(400, "性别参数不合法");
            }
        }

        // birthday 非空时必须为合法日期
        String birthday = dto.getBirthday();
        if (birthday != null && !birthday.trim().isEmpty()) {
            try {
                LocalDate.parse(birthday.trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new BaseException(400, "出生日期格式错误");
            }
        }

        // phone 非空时必须匹配手机号格式
        String phone = dto.getPhone();
        if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new BaseException(400, "手机号格式不正确");
        }

        // email 非空时必须匹配邮箱格式
        String email = dto.getEmail();
        if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new BaseException(400, "邮箱格式不正确");
        }

        // bio 最长 300 字
        String bio = dto.getBio();
        if (bio != null && bio.length() > 300) {
            throw new BaseException(400, "个人简介不能超过300个字符");
        }
    }

    /*
     * 字符串 trim 后，空串转 null，保持数据库一致性
     */
    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
