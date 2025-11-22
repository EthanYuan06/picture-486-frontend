package com.yuluo.picture486backend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486backend.annotation.AuthCheck;
import com.yuluo.picture486backend.common.BaseResponse;
import com.yuluo.picture486backend.common.DeleteRequest;
import com.yuluo.picture486backend.common.ResultUtils;
import com.yuluo.picture486backend.constant.UserConstant;
import com.yuluo.picture486backend.exception.BusinessException;
import com.yuluo.picture486backend.exception.ErrorCode;
import com.yuluo.picture486backend.exception.ThrowUtils;
import com.yuluo.picture486backend.manager.CosManager;
import com.yuluo.picture486backend.manager.upload.FilePictureUpload;
import com.yuluo.picture486backend.model.dto.picture.PictureUploadResult;
import com.yuluo.picture486backend.model.dto.user.*;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.vo.LoginUserVo;
import com.yuluo.picture486backend.model.vo.UserVo;
import com.yuluo.picture486backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "用户模块")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private FilePictureUpload filePictureUpload;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public BaseResponse<LoginUserVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVo loginUserVo = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVo);
    }

    @GetMapping("/get/login")
    @Operation(summary = "获取当前用户")
    public BaseResponse<LoginUserVo> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVo(loginUser));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public BaseResponse<Boolean> userLogin(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @PostMapping("/reset")
    @Operation(summary = "重置密码")
    public BaseResponse<Boolean> userReset(@RequestBody UserResetRequest userResetRequest) {
        ThrowUtils.throwIf(userResetRequest == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //根据账户检查数据库是否有该用户
        queryWrapper.eq("userAccount", userResetRequest.getUserAccount());
        User resetUser = userService.getOne(queryWrapper);
        //校验输入的账户密码是否有效
        String userAccount = userResetRequest.getUserAccount();
        String passWord = userResetRequest.getUserPassword();
        String checkPassword = userResetRequest.getCheckPassword();
        userService.validKey(userAccount, passWord, checkPassword);
        //设置重置用户的加密密码
        String encryptedPassword = userService.getEncryptedPassword(userResetRequest.getUserPassword());
        resetUser.setUserPassword(encryptedPassword);
        boolean result = userService.updateById(resetUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/add")
    @Operation(summary = "【管理员】创建用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        //默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        user.setUserPassword(userService.getEncryptedPassword(DEFAULT_PASSWORD));
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    @GetMapping("/get")
    @Operation(summary = "【管理员】根据id获取当前用户（未脱敏）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    @GetMapping("/get/vo")
    @Operation(summary = "根据id获取当前用户信息（脱敏）")
    public BaseResponse<UserVo> getUserVoById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVo(user));
    }

    @PostMapping("/delete")
    @Operation(summary = "【管理员】删除用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    @PostMapping("/update")
    @Operation(summary = "【管理员】更新用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    @PostMapping("/list/page/vo")
    @Operation(summary = "【管理员】分页获取用户封装列表")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVo>> listUserVoByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null , ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();//当前页
        long pageSize = userQueryRequest.getPageSize();//每页大小
        //获取查询参数
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        //获取结果
        Page<UserVo> userVoPage = new Page<>(current, pageSize, userPage.getTotal());
        //获取用户列表
        List<UserVo> userVoList = userService.getUserVoList(userPage.getRecords());
        //设置结果
        userVoPage.setRecords(userVoList);
        return ResultUtils.success(userVoPage);
    }

    @PostMapping("/avatar/upload")
    @Operation(summary = "用户上传头像")
    public BaseResponse<Boolean> uploadAvatar(@RequestPart("file") MultipartFile file, HttpServletRequest request, @RequestParam("id") Long id) {
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser.getUserRole().equals(UserConstant.DEFAULT_ROLE)) {
            // 构建上传路径前缀，指定存储桶中的存储路径为 avatar 目录
            String uploadPathPrefix = String.format("avatar/%s", loginUser.getId());
            // 使用文件上传处理器
            PictureUploadResult uploadResult = filePictureUpload.uploadPicture(file, uploadPathPrefix);
            // 只更新用户头像URL字段
            User updateUser = new User();
            updateUser.setId(loginUser.getId());
            updateUser.setUserAvatar(uploadResult.getUrl());
            boolean result = userService.updateById(updateUser);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "头像上传失败");
        }
        if (loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            // 构建上传路径前缀，指定存储桶中的存储路径为 avatar 目录
            String uploadPathPrefix = String.format("avatar/%s", id);
            // 使用文件上传处理器
            PictureUploadResult uploadResult = filePictureUpload.uploadPicture(file, uploadPathPrefix);
            // 更新用户头像
            User updateUser = new User();
            updateUser.setId(id);
            updateUser.setUserAvatar(uploadResult.getUrl());
            boolean result = userService.updateById(updateUser);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "头像上传失败");
        }
        return ResultUtils.success(true);
    }


















}
