package com.yuluo.picture486ddd.interfaces.controller;

import cn.hutool.core.util.ObjectUtil;
import com.yuluo.picture486ddd.application.service.SpaceUserApplicationService;
import com.yuluo.picture486ddd.infrastructure.common.BaseResponse;
import com.yuluo.picture486ddd.infrastructure.common.DeleteRequest;
import com.yuluo.picture486ddd.infrastructure.common.ResultUtils;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486ddd.infrastructure.manager.auth.annotation.SaSpaceCheckPermission;
import com.yuluo.picture486ddd.infrastructure.manager.auth.model.SpaceUserPermissionConstant;
import com.yuluo.picture486ddd.interfaces.assembler.SpaceUserAssembler;
import com.yuluo.picture486ddd.interfaces.dto.space_user.SpaceUserAddRequest;
import com.yuluo.picture486ddd.interfaces.dto.space_user.SpaceUserEditRequest;
import com.yuluo.picture486ddd.interfaces.dto.space_user.SpaceUserQueryRequest;
import com.yuluo.picture486ddd.domain.space.entity.SpaceUser;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.interfaces.vo.space.SpaceUserVo;
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
    private SpaceUserApplicationService spaceUserApplicationService;

    @PostMapping("/add")
    @Operation(summary = "添加成员到相册")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest) {
        long id = spaceUserApplicationService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(id);
    }

    @PostMapping("/delete")
    @Operation(summary = "从相册中移除成员")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest) {
        return ResultUtils.success(spaceUserApplicationService.deleteSpaceUser(deleteRequest));
    }

    @PostMapping("/get/vo")
    @Operation(summary = "查询某个成员在某个相册中的信息")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUserVo> getSpaceUserVo(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest, HttpServletRequest request) {
        SpaceUser spaceUser = SpaceUserAssembler.toSpaceUserEntity(spaceUserQueryRequest);
        return ResultUtils.success(spaceUserApplicationService.getSpaceUserVo(spaceUser, request));
    }

    @PostMapping("/list")
    @Operation(summary = "查询相册成员列表")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVo>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        List<SpaceUser> spaceUserList = spaceUserApplicationService.listSpaceUser(spaceUserQueryRequest);
        return ResultUtils.success(spaceUserApplicationService.getSpaceUserVoList(spaceUserList));
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑成员信息")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest) {
        spaceUserApplicationService.editSpaceUser(spaceUserEditRequest);
        return ResultUtils.success(true);
    }
    
    @PostMapping("/list/me")
    @Operation(summary = "查询我加入的多人相册列表（所有已登录用户）")
    public BaseResponse<List<SpaceUserVo>> listMyTeamSpace(HttpServletRequest request) {
        List<SpaceUser> spaceUserList = spaceUserApplicationService.listMyTeamSpace(request);
        return ResultUtils.success(spaceUserApplicationService.getSpaceUserVoList(spaceUserList));
    }
}
