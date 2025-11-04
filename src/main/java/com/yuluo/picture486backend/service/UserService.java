package com.yuluo.picture486backend.service;

import com.yuluo.picture486backend.model.dto.UserRegisterRequest;
import com.yuluo.picture486backend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuluo.picture486backend.model.vo.LoginUserVo;
import jakarta.servlet.http.HttpServletRequest;

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
    long userRegister(UserRegisterRequest userRegisterRequest);


    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      请求
     * @return 登录成功用户信息
     */
    LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏登录用户信息
     *
     * @param user 用户信息
     * @return 脱敏登录用户信息
     */
    LoginUserVo getLoginUserVo(User user);

    /**
     * 获取加密密码
     *
     * @param userPassword 密码
     * @return 加密后的密码
     */
    String getEncryptedPassword(String userPassword);
}
