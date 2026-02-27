package com.yuluo.picture486backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486ddd.infrastructure.annotation.AuthCheck;
import com.yuluo.picture486ddd.infrastructure.common.BaseResponse;
import com.yuluo.picture486ddd.infrastructure.common.DeleteRequest;
import com.yuluo.picture486ddd.infrastructure.common.ResultUtils;
import com.yuluo.picture486ddd.domain.user.constant.UserConstant;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486ddd.infrastructure.manager.auth.SpaceUserAuthManager;
import com.yuluo.picture486backend.model.dto.space.*;
import com.yuluo.picture486backend.model.entity.Space;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486backend.model.enums.SpaceLevelEnum;
import com.yuluo.picture486backend.model.vo.SpaceVo;
import com.yuluo.picture486ddd.domain.picture.service.PictureDomainService;
import com.yuluo.picture486backend.service.SpaceService;
import com.yuluo.picture486ddd.application.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/space")
@Tag(name = "相册模块")
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private PictureDomainService pictureDomainService;
    @Autowired
    private SpaceUserAuthManager spaceUserAuthManager;


    @PostMapping("/add")
    @Operation(summary = "创建相册")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        if (spaceAddRequest == null || request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userApplicationService.getLoginUser(request);
        return ResultUtils.success(spaceService.addSpace(spaceAddRequest, loginUser));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除相册")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userApplicationService.getLoginUser(request);
        Long spaceId = deleteRequest.getId();
        //判断相册是否存在
        Space oldSpace = spaceService.getById(spaceId);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        //仅本人或管理员可删除
        spaceService.checkSpaceAuth(oldSpace, loginUser);
        //判断用户是否输入了确认删除的信息，管理员不需要输入确认文本
        if (!User.isAdmin(loginUser)){
            ThrowUtils.throwIf(deleteRequest.getDelConfirmInfo() == null, ErrorCode.PARAMS_ERROR, "请输入确认删除文本");
            if (!deleteRequest.getDelConfirmInfo().equals("我确定要删除此相册")){
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "请输入指定的文本");
            }
        }
        //先删除该相册下的所有图片
        List<Long> pictureIdsToDel = pictureDomainService.getPictureIds(spaceId);
        Boolean isDelPictures = pictureDomainService.deletePictures(pictureIdsToDel, loginUser);
        ThrowUtils.throwIf(!isDelPictures, ErrorCode.OPERATION_ERROR, "删除相册所有图片失败");
        //操作数据库删除相册
        boolean isDelSpace = spaceService.removeById(spaceId);
        ThrowUtils.throwIf(!isDelSpace, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @Operation(summary = "【管理员】更新相册")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //将实体类和DTO进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        //填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        //数据校验
        spaceService.validSpace(space,false);
        //判断相册是否存在
        Long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        //操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "【管理员】根据id获取相册")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(space);
    }

    @GetMapping("/get/vo")
    @Operation(summary = "根据id获取相册（脱敏）")
    public BaseResponse<SpaceVo> getSpaceVoById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        SpaceVo spaceVo = spaceService.getSpaceVo(space, request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        spaceVo.setPermissionList(permissionList);
        return ResultUtils.success(spaceVo);
    }

    @PostMapping("/list/page")
    @Operation(summary = "【管理员】分页获取相册列表")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();

        //查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));

        return ResultUtils.success(spacePage);
    }

    @PostMapping("/list/page/vo")
    @Operation(summary = "分页获取相册列表")
    public BaseResponse<Page<SpaceVo>> listSpaceVoByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        
        //查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size), spaceService.getQueryWrapper(spaceQueryRequest));
        Page<SpaceVo> spaceVoPage = spaceService.getSpaceVoPage(spacePage, request);

        return ResultUtils.success(spaceVoPage);
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑相册")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //实体类和DTO进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        //填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        //设置编辑时间
        space.setEditTime(new Date());
        //数据校验
        spaceService.validSpace(space, false);
        User loginUser = userApplicationService.getLoginUser(request);
        //判断相册是否存在
        Long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        //仅本人或管理员可编辑
        spaceService.checkSpaceAuth(oldSpace, loginUser);
        //操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
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