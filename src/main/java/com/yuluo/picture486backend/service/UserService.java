package com.yuluo.picture486backend.service;

import com.yuluo.picture486backend.model.dto.UserRegisterRequest;
import com.yuluo.picture486backend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 东山千夏
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-11-04 09:58:57
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userRegisterRequest 注册信息
     * @return 注册成功用户id
     */
    public long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 获取加密密码
     *
     * @param userPassword 密码
     * @return 加密后的密码
     */
    String getEncryptedPassword(String userPassword);
}
