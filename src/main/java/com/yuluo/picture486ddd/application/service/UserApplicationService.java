package com.yuluo.picture486ddd.application.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486ddd.infrastructure.common.DeleteRequest;
import com.yuluo.picture486ddd.interfaces.dto.user.*;
import com.yuluo.picture486ddd.domain.user.entity.User;

import com.yuluo.picture486ddd.interfaces.vo.user.LoginUserVo;
import com.yuluo.picture486ddd.interfaces.vo.user.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author 东山羽洛
 */
public interface UserApplicationService {
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
     * @param userLoginRequest 登录信息
     * @return 登录成功用户信息
     */
    LoginUserVo userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

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
     * 【管理员】获取单个用户信息
     * @param id
     * @return
     */
    User getUser(long id);

    /**
     * 获取单个脱敏用户信息
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


    UserVo getUserVoById(long id);

    boolean deleteUser(DeleteRequest deleteRequest);

    boolean updateUser(User user, HttpServletRequest request);

    Page<UserVo> listUserVoByPage(UserQueryRequest userQueryRequest);

    List<User> listByIds(Set<Long> userIdSet);

    Boolean resetPassword(UserResetRequest userResetRequest);

    long addUser(UserAddRequest userAddRequest);

    void uploadAvatar(MultipartFile file, HttpServletRequest request, Long id);

    Page<User> listUserByPage(UserQueryRequest userQueryRequest);
}
