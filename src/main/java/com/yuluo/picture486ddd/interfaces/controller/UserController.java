package com.yuluo.picture486ddd.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486ddd.infrastructure.annotation.AuthCheck;
import com.yuluo.picture486ddd.infrastructure.common.BaseResponse;
import com.yuluo.picture486ddd.infrastructure.common.DeleteRequest;
import com.yuluo.picture486ddd.infrastructure.common.ResultUtils;
import com.yuluo.picture486ddd.domain.user.constant.UserConstant;
import com.yuluo.picture486ddd.interfaces.assembler.UserAssembler;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.interfaces.dto.user.*;
import com.yuluo.picture486ddd.interfaces.vo.user.LoginUserVo;
import com.yuluo.picture486ddd.interfaces.vo.user.UserVo;
import com.yuluo.picture486ddd.application.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@Tag(name = "用户模块")
public class UserController {
    @Resource
    private UserApplicationService userApplicationService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        long result = userApplicationService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public BaseResponse<LoginUserVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        LoginUserVo loginUserVo = userApplicationService.userLogin(userLoginRequest, request);
        return ResultUtils.success(loginUserVo);
    }

    @GetMapping("/get/login")
    @Operation(summary = "获取当前用户")
    public BaseResponse<LoginUserVo> getLoginUser(HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        return ResultUtils.success(userApplicationService.getLoginUserVo(loginUser));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = userApplicationService.userLogout(request);
        return ResultUtils.success(result);
    }

    @PostMapping("/reset")
    @Operation(summary = "重置密码")
    public BaseResponse<Boolean> userReset(@RequestBody UserResetRequest userResetRequest) {
        Boolean result = userApplicationService.resetPassword(userResetRequest);
        return ResultUtils.success(result);
    }

    @PostMapping("/add")
    @Operation(summary = "【管理员】创建用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        return ResultUtils.success(userApplicationService.addUser(userAddRequest));
    }

    @GetMapping("/get")
    @Operation(summary = "【管理员】根据id获取当前用户（未脱敏）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        return ResultUtils.success(userApplicationService.getUser(id));
    }

    @GetMapping("/get/vo")
    @Operation(summary = "根据id获取当前用户信息（脱敏）")
    public BaseResponse<UserVo> getUserVoById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userApplicationService.getUserVo(user));
    }

    @PostMapping("/delete")
    @Operation(summary = "【管理员】删除用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        boolean b = userApplicationService.deleteUser(deleteRequest);
        return ResultUtils.success(b);
    }

    @PostMapping("/update")
    @Operation(summary = "更新用户信息")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        return ResultUtils.success(userApplicationService.updateUser(userUpdateRequest, request));
    }

    @PostMapping("/list/page/vo")
    @Operation(summary = "分页获取用户封装列表（脱敏）")
    public BaseResponse<Page<UserVo>> listUserVoByPage(@RequestBody UserQueryRequest userQueryRequest) {
        return ResultUtils.success(userApplicationService.listUserVoByPage(userQueryRequest));
    }

    @PostMapping("/list/page")
    @Operation(summary = "【管理员】分页获取用户封装列表")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        return ResultUtils.success(userApplicationService.listUserByPage(userQueryRequest));
    }

    @PostMapping("/avatar/upload")
    @Operation(summary = "用户上传头像")
    public BaseResponse<Boolean> uploadAvatar(@RequestPart("file") MultipartFile file, HttpServletRequest request, @RequestParam("id") Long id) {
        userApplicationService.uploadAvatar(file,request,id);
        return ResultUtils.success(true);
    }


















}
