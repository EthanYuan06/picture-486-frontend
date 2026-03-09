package com.yuluo.picture486ddd.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486ddd.application.service.SpaceApplicationService;
import com.yuluo.picture486ddd.infrastructure.annotation.AuthCheck;
import com.yuluo.picture486ddd.infrastructure.common.BaseResponse;
import com.yuluo.picture486ddd.infrastructure.common.DeleteRequest;
import com.yuluo.picture486ddd.infrastructure.common.ResultUtils;
import com.yuluo.picture486ddd.domain.user.constant.UserConstant;
import com.yuluo.picture486ddd.domain.space.entity.Space;
import com.yuluo.picture486ddd.domain.space.valueobject.SpaceLevelEnum;
import com.yuluo.picture486ddd.interfaces.vo.space.SpaceVo;
import com.yuluo.picture486ddd.interfaces.dto.space.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/space")
@Tag(name = "相册模块")
public class SpaceController {

    @Resource
    private SpaceApplicationService spaceApplicationService;
    @PostMapping("/add")
    @Operation(summary = "创建相册")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        return ResultUtils.success(spaceApplicationService.addSpace(spaceAddRequest, request));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除相册")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        spaceApplicationService.deleteSpace(deleteRequest, request);
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @Operation(summary = "【管理员】更新相册")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        spaceApplicationService.updateSpace(spaceUpdateRequest);
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "【管理员】根据id获取相册")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id) {
        return ResultUtils.success(spaceApplicationService.getSpaceById(id));
    }

    @GetMapping("/get/vo")
    @Operation(summary = "根据id获取相册（脱敏）")
    public BaseResponse<SpaceVo> getSpaceVoById(long id, HttpServletRequest request) {
        SpaceVo spaceVo = spaceApplicationService.getSpaceVoById(id, request);
        return ResultUtils.success(spaceVo);
    }

    @PostMapping("/list/page")
    @Operation(summary = "【管理员】分页获取相册列表")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        Page<Space> spacePage = spaceApplicationService.listSpaceByPage(spaceQueryRequest);
        return ResultUtils.success(spacePage);
    }

    @PostMapping("/list/page/vo")
    @Operation(summary = "分页获取相册列表")
    public BaseResponse<Page<SpaceVo>> listSpaceVoByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        Page<SpaceVo> spaceVoPage = spaceApplicationService.listSpaceVoByPage(spaceQueryRequest, request);
        return ResultUtils.success(spaceVoPage);
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑相册")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        spaceApplicationService.editSpace(spaceEditRequest, request);
        return ResultUtils.success(true);
    }

    @GetMapping("/list/level")
    @Operation(summary = "获取相册等级列表")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values()) // 获取所有枚举
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }














}