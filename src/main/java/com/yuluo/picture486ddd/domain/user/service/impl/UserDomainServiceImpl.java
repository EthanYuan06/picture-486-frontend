package com.yuluo.picture486ddd.domain.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486ddd.domain.user.constant.UserConstant;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.domain.user.repository.UserRepository;
import com.yuluo.picture486ddd.domain.user.service.UserDomainService;
import com.yuluo.picture486ddd.domain.user.valueobject.UserRoleEnum;
import com.yuluo.picture486ddd.infrastructure.common.DeleteRequest;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486ddd.interfaces.assembler.UserAssembler;
import com.yuluo.picture486ddd.interfaces.dto.user.UserAddRequest;
import com.yuluo.picture486ddd.interfaces.dto.user.UserQueryRequest;
import com.yuluo.picture486ddd.interfaces.dto.user.UserRegisterRequest;
import com.yuluo.picture486ddd.interfaces.dto.user.UserResetRequest;
import com.yuluo.picture486ddd.interfaces.vo.user.LoginUserVo;
import com.yuluo.picture486ddd.interfaces.vo.user.UserVo;
import com.yuluo.picture486ddd.infrastructure.manager.auth.StpKit;
import com.yuluo.picture486ddd.infrastructure.manager.upload.FilePictureUpload;
import com.yuluo.picture486ddd.interfaces.dto.picture.PictureUploadResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yuluo.picture486ddd.domain.user.constant.UserConstant.USER_LOGIN_STATE;
import static com.yuluo.picture486ddd.domain.user.entity.User.validKey;

/**
 * @author 东山羽洛
 */
@Service
@Slf4j
public class UserDomainServiceImpl implements UserDomainService {

    @Resource
    private UserRepository userRepository;
    @Resource
    private FilePictureUpload filePictureUpload;

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
        //2.加密
        String encryptedPassword = getEncryptedPassword(userPassword);
        //3.插入数据
        User user = UserAssembler.toUserEntity(userRegisterRequest);
        user.setUserAccount(userAccount);
        user.setUserEmail(userEmail);
        user.setUserPassword(encryptedPassword);
        user.setUserName("昴云N号用户");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = userRepository.save(user);
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

        //2.加密
        String encryptedPassword = getEncryptedPassword(userPassword);
        //3.查询用户是否存在（防止缓存与数据库不一致）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptedPassword);
        User user = userRepository.getBaseMapper().selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword.");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (attribute == null) {
            //4.记录用户登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            //5.记录用户登录态到Sa-Token中，便于多人相册鉴权
            StpKit.SPACE.login(user.getId());//同时登录Space体系的账号（分为User体系用于整个项目，和Space体系用于多人相册）
            StpKit.SPACE.getSession().set(USER_LOGIN_STATE, user);//记录Space体系的登录态
        }
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
        Object user = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        //2.移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        //3.移除Sa-Token登录态
        StpKit.SPACE.logout();
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
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //2.数据库查询用户是否存在
        long userId = currentUser.getId();
        currentUser = userRepository.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
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

    @Override
    public User getUser(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userRepository.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return user;
    }

    /**
     * 获取单个脱敏用户信息
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
     * 获取脱敏用户列表
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

    @Override
    public long addUser(UserAddRequest userAddRequest){
        User user = UserAssembler.toUserEntity(userAddRequest);
        //默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        user.setUserPassword(this.getEncryptedPassword(DEFAULT_PASSWORD));
        boolean result = userRepository.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return user.getId();
    }

    @Override
    public void uploadAvatar(MultipartFile file, HttpServletRequest request, Long id) {
        // 获取登录用户
        User loginUser = this.getLoginUser(request);
        // 校验头像文件是否超过限制：大小不超过1MB
        ThrowUtils.throwIf(file.getSize() > 1024 * 1024, ErrorCode.PARAMS_ERROR, "头像文件不符合要求");
        if (loginUser.getUserRole().equals(UserConstant.DEFAULT_ROLE)) {
            // 构建上传路径前缀，指定存储桶中的存储路径为 avatar 目录
            String uploadPathPrefix = String.format("avatar/%s", loginUser.getId());
            // 使用文件上传处理器
            PictureUploadResult uploadResult = filePictureUpload.uploadPicture(file, uploadPathPrefix);
            // 只更新用户头像URL字段
            User updateUser = new User();
            updateUser.setId(loginUser.getId());
            updateUser.setUserAvatar(uploadResult.getUrl());
            boolean result = userRepository.updateById(updateUser);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "头像上传失败");
        }
        if (loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            // 构建上传路径前缀，指定存储桶中的存储路径为 avatar 目录
            String uploadPathPrefix = String.format("avatar/%s", id);
            // 使用文件上传处理器
            PictureUploadResult uploadResult = filePictureUpload.uploadPicture(file, uploadPathPrefix);
            // 只更新用户头像URL字段
            User updateUser = new User();
            updateUser.setId(id);
            updateUser.setUserAvatar(uploadResult.getUrl());
            boolean result = userRepository.updateById(updateUser);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "头像上传失败");
        }
    }

    @Override
    public boolean deleteUser(DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userRepository.removeById(deleteRequest.getId());
    }

    @Override
    public boolean updateUser(User user, HttpServletRequest request) {
        //用户只能修改自己的信息，管理员可以修改任意用户的信息
        if (!UserRoleEnum.ADMIN.getValue().equals(user.getUserRole()) && !user.getId().equals(getLoginUser(request).getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = userRepository.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    @Override
    public Page<UserVo> listUserVoByPage(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userRepository.page(new Page<>(current, size),
                this.getQueryWrapper(userQueryRequest));
        Page<UserVo> userVoPage = new Page<>(current, size, userPage.getTotal());
        List<UserVo> userVoList = this.getUserVoList(userPage.getRecords());
        userVoPage.setRecords(userVoList);
        return userVoPage;
    }

    @Override
    public Page<User> listUserByPage(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        return userRepository.page(new Page<>(current, size), this.getQueryWrapper(userQueryRequest));
    }

    @Override
    public List<User> listByIds(Set<Long> userIdSet) {
        return userRepository.listByIds(userIdSet);
    }

    @Override
    public Boolean resetPassword(UserResetRequest userResetRequest) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //根据账户检查数据库是否有该用户
        queryWrapper.eq("userAccount", userResetRequest.getUserAccount());
        User resetUser = userRepository.getOne(queryWrapper);
        //校验输入的账户密码是否有效
        String userAccount = userResetRequest.getUserAccount();
        String passWord = userResetRequest.getUserPassword();
        String checkPassword = userResetRequest.getCheckPassword();
        validKey(userAccount, passWord, checkPassword);
        //设置重置用户的加密密码
        String encryptedPassword = this.getEncryptedPassword(userResetRequest.getUserPassword());
        resetUser.setUserPassword(encryptedPassword);
        return userRepository.updateById(resetUser);
    }




}