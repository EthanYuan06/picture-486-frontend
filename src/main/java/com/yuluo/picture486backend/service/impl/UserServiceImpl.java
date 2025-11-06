package com.yuluo.picture486backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486backend.constant.UserConstant;
import com.yuluo.picture486backend.exception.BusinessException;
import com.yuluo.picture486backend.exception.ErrorCode;
import com.yuluo.picture486backend.model.dto.user.UserQueryRequest;
import com.yuluo.picture486backend.model.dto.user.UserRegisterRequest;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.enums.UserRoleEnum;
import com.yuluo.picture486backend.model.vo.LoginUserVo;
import com.yuluo.picture486backend.model.vo.UserVo;
import com.yuluo.picture486backend.service.UserService;
import com.yuluo.picture486backend.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
* @author 东山千夏
* @ description 针对表【user(用户)】的数据库操作Service实现
* @ createDate 2025-11-04 09:58:57
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    /**
     * 用户注册
     *
     * @param userRegisterRequest 注册信息
     * @return 注册成功用户id
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userEmail = userRegisterRequest.getUserEmail();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        
        //1.校验参数与账号密码
        if (StringUtils.isAnyBlank(userAccount, userEmail, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名长度应为4-20个字符");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于8个字符");
        }

        // 账户必须包含字母和数字，且不能以数字开头
        if (!Character.isLetter(userAccount.charAt(0))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能以数字开头");
        }
        if (!userAccount.matches(".*[a-zA-Z]+.*") || !userAccount.matches(".*\\d+.*")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号必须同时包含字母和数字");
        }

        // 密码必须包含大小写字母和数字
        if (!userPassword.matches(".*[a-z]+.*") || !userPassword.matches(".*[A-Z]+.*") || !userPassword.matches(".*\\d+.*")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码必须包含大小写字母和数字");
        }

        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 校验邮箱格式
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@((?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,})$";
        Pattern emailRegex = Pattern.compile(emailPattern);
        Matcher emailMatcher = emailRegex.matcher(userEmail);
        if (!emailMatcher.matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入有效的邮箱地址");
        }

        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }

        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已被注册");
        }

        //2.加密
        String encryptedPassword = getEncryptedPassword(userPassword);
        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserEmail(userEmail);
        user.setUserPassword(encryptedPassword);
        user.setUserName("云图库N号用户");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求
     * @return 登录成功用户信息
     */
    @Override
    public LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验账号密码
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        //2.加密
        String encryptedPassword = getEncryptedPassword(userPassword);
        //3.查询用户是否存在（防止缓存与数据库不一致）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptedPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword.");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //4.记录用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVo(user);
    }

    /**
     * 退出登录
     *
     * @param request 请求
     * @return 退出成功
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        //1.判断是否登录
        Object user = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        //2.移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        //1.判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //2.数据库查询用户是否存在
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取脱敏登录用户信息
     *
     * @param user 用户信息
     * @return 脱敏登录用户信息
     */
    public LoginUserVo getLoginUserVo(User user) {
        if (user == null){
            return null;
        }
        LoginUserVo loginUserVo = new LoginUserVo();
        BeanUtil.copyProperties(user, loginUserVo);
        return loginUserVo;
    }


    /**
     * 获取加密密码
     *
     * @param userPassword 密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptedPassword(String userPassword){
        String SALT = "I love AwaSubaru";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 【管理员】获取单个脱敏用户信息
     *
     * @param user 用户信息
     * @return 脱敏用户信息
     */
    @Override
    public UserVo getUserVo(User user) {
        if (user == null){
            return null;
        }
        UserVo userVo = new UserVo();
        BeanUtil.copyProperties(user, userVo);
        return userVo;
    }

    /**
     * 【管理员】获取脱敏用户列表
     *
     * @param userList 用户列表
     * @return 脱敏用户列表
     */
    @Override
    public List<UserVo> getUserVoList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVo).collect(Collectors.toList());
    }

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询条件
     * @return 查询条件
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        //获取参数
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userEmail = userQueryRequest.getUserEmail();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        //创建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userEmail), "userEmail", userEmail);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }



}