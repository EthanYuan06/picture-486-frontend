package com.yuluo.picture486backend.controller;

import cn.hutool.core.util.ObjectUtil;
import com.yuluo.picture486ddd.infrastructure.common.BaseResponse;
import com.yuluo.picture486ddd.infrastructure.common.DeleteRequest;
import com.yuluo.picture486ddd.infrastructure.common.ResultUtils;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486backend.manager.auth.annotation.SaSpaceCheckPermission;
import com.yuluo.picture486backend.manager.auth.model.SpaceUserPermissionConstant;
import com.yuluo.picture486backend.model.dto.space_user.SpaceUserAddRequest;
import com.yuluo.picture486backend.model.dto.space_user.SpaceUserEditRequest;
import com.yuluo.picture486backend.model.dto.space_user.SpaceUserQueryRequest;
import com.yuluo.picture486backend.model.entity.SpaceUser;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486backend.model.vo.space_user.SpaceUserVo;
import com.yuluo.picture486backend.service.SpaceUserService;
import com.yuluo.picture486ddd.application.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/spaceUser")
@Tag(name = "多人相册成员模块")
@Slf4j
public class SpaceUserController {
    @Resource
    private SpaceUserService spaceUserService;
    @Resource
    private UserApplicationService userApplicationService;

    @PostMapping("/add")
    @Operation(summary = "添加成员到相册")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        long id = spaceUserService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(id);
    }

    @PostMapping("/delete")
    @Operation(summary = "从相册中移除成员")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        //判断是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        //删除
        boolean result = spaceUserService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/get")
    @Operation(summary = "查询某个成员在某个相册中的信息")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
        //查询
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    @PostMapping("/list")
    @Operation(summary = "查询相册成员列表")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVo>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVoList(spaceUserList));
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑成员信息")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest) {
        if (spaceUserEditRequest == null || spaceUserEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //将实体类和DTO进行转换
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserEditRequest, spaceUser);
        //判断是否存在
        Long id = spaceUserEditRequest.getId();
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        //编辑
        boolean result = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    
    @PostMapping("/list/me")
    @Operation(summary = "查询我加入的多人相册列表（所有已登录用户）")
    public BaseResponse<List<SpaceUserVo>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVoList(spaceUserList));
    }
}
