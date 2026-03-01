package com.yuluo.picture486ddd.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486ddd.domain.user.repository.UserRepository;
import com.yuluo.picture486ddd.domain.user.service.UserDomainService;
import com.yuluo.picture486ddd.infrastructure.common.DeleteRequest;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.interfaces.dto.user.*;
import com.yuluo.picture486ddd.interfaces.vo.user.LoginUserVo;
import com.yuluo.picture486ddd.interfaces.vo.user.UserVo;
import com.yuluo.picture486ddd.application.service.UserApplicationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author 东山羽洛
 */
@Service
@Slf4j
public class UserApplicationServiceImpl implements UserApplicationService {

    @Resource
    private UserDomainService userDomainService;
    @Resource
    private UserRepository userRepository;
    /**
     * 用户注册
     *
     * @param userRegisterRequest 注册信息
     * @return 注册成功用户id
     */
    @Override
    @Transactional
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userEmail = userRegisterRequest.getUserEmail();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        //校验基本格式
        User.validUserRegister(userAccount, userEmail, userPassword, checkPassword);
        //校验账户唯一性
        userRepository.existAccount(userAccount);
        return userDomainService.userRegister(userRegisterRequest);
    }

    /**
     * 用户登录
     * @param request 请求
     * @return 登录成功用户信息
     */
    @Override
    public LoginUserVo userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        //校验
        User.validUserLogin(userAccount, userPassword);
        return userDomainService.userLogin(userAccount, userPassword, request);
    }

    /**
     * 退出登录
     *
     * @param request 请求
     * @return 退出成功
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        return userDomainService.userLogout(request);
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        return userDomainService.getLoginUser(request);
    }

    /**
     * 获取脱敏登录用户信息
     *
     * @param user 用户信息
     * @return 脱敏登录用户信息
     */
    public LoginUserVo getLoginUserVo(User user) {
        return userDomainService.getLoginUserVo(user);
    }


    /**
     * 【管理员】获取单个脱敏用户信息
     *
     * @param user 用户信息
     * @return 脱敏用户信息
     */
    @Override
    public UserVo getUserVo(User user) {
        return userDomainService.getUserVo(user);
    }

    @Override
    public User getUser(long id) {
        return userDomainService.getUser(id);
    }

    /**
     * 【管理员】获取脱敏用户列表
     *
     * @param userList 用户列表
     * @return 脱敏用户列表
     */
    @Override
    public List<UserVo> getUserVoList(List<User> userList) {
       return userDomainService.getUserVoList(userList);
    }

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询条件
     * @return 查询条件
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        return userDomainService.getQueryWrapper(userQueryRequest);
    }



    @Override
    public UserVo getUserVoById(long id) {
        return userDomainService.getUserVo(getUser(id));
    }

    @Override
    public boolean deleteUser(DeleteRequest deleteRequest) {
        return userDomainService.deleteUser(deleteRequest);
    }

    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        User user = this.getLoginUser(request);
        return userDomainService.updateUser(user, userUpdateRequest, request);
    }

    @Override
    public Page<UserVo> listUserVoByPage(UserQueryRequest userQueryRequest) {
        return userDomainService.listUserVoByPage(userQueryRequest);
    }

    @Override
    public Page<User> listUserByPage(UserQueryRequest userQueryRequest) {
        return userDomainService.listUserByPage(userQueryRequest);
    }

    @Override
    public List<User> listByIds(Set<Long> userIdSet) {
        return userDomainService.listByIds(userIdSet);
    }

    @Override
    public Boolean resetPassword(UserResetRequest userResetRequest) {
        return userDomainService.resetPassword(userResetRequest);
    }

    @Override
    public long addUser(UserAddRequest userAddRequest) {
        return userDomainService.addUser(userAddRequest);
    }

    @Override
    public void uploadAvatar(MultipartFile file, HttpServletRequest request, Long id) {
        userDomainService.uploadAvatar(file, request, id);

    }


}