package com.yuluo.picture486backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuluo.picture486backend.model.dto.user.UserQueryRequest;
import com.yuluo.picture486backend.model.dto.user.UserRegisterRequest;
import com.yuluo.picture486backend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuluo.picture486backend.model.vo.LoginUserVo;
import com.yuluo.picture486backend.model.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

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
     * 用户登出
     * @param request 请求
     * @return 是否登出成功
     */
    boolean userLogout(HttpServletRequest request);

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

    /**
     * 【管理员】获取单个脱敏用户信息
     *
     * @param user 用户信息
     * @return 脱敏用户信息
     */
    UserVo getUserVo(User user);

    /**
     * 【管理员】获取脱敏用户列表
     *
     * @param userList 用户列表
     * @return 脱敏用户列表
     */
    List<UserVo> getUserVoList(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 校验管理员身份
     *
     * @param user 用户信息
     * @return 是否为管理员
     */
    boolean isAdmin(User user);































}
