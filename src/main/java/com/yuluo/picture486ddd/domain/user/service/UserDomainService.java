package com.yuluo.picture486ddd.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.infrastructure.common.DeleteRequest;
import com.yuluo.picture486ddd.interfaces.dto.user.UserAddRequest;
import com.yuluo.picture486ddd.interfaces.dto.user.UserQueryRequest;
import com.yuluo.picture486ddd.interfaces.dto.user.UserRegisterRequest;
import com.yuluo.picture486ddd.interfaces.dto.user.UserResetRequest;
import com.yuluo.picture486ddd.interfaces.vo.user.LoginUserVo;
import com.yuluo.picture486ddd.interfaces.vo.user.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author 东山羽洛
 */
public interface UserDomainService {
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

    User getUser(long id);

    boolean deleteUser(DeleteRequest deleteRequest);

    boolean updateUser(User user, HttpServletRequest request);

    Page<UserVo> listUserVoByPage(UserQueryRequest userQueryRequest);

    Page<User> listUserByPage(UserQueryRequest userQueryRequest);

    List<User> listByIds(Set<Long> userIdSet);

    Boolean resetPassword(UserResetRequest userResetRequest);

    long addUser(UserAddRequest userAddRequest);

    void uploadAvatar(MultipartFile file, HttpServletRequest request, Long id);


}
