package com.yuluo.picture486ddd.interfaces.controller;

import com.yuluo.picture486ddd.infrastructure.common.BaseResponse;
import com.yuluo.picture486ddd.infrastructure.common.ResultUtils;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486ddd.domain.space.entity.Space;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.application.service.SpaceAnalyzeApplicationService;
import com.yuluo.picture486ddd.application.service.UserApplicationService;
import com.yuluo.picture486ddd.interfaces.dto.space.analyze.*;
import com.yuluo.picture486ddd.interfaces.vo.space.analyze.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/space/analyze")
@Tag(name = "分析模块")
@Slf4j
public class SpaceAnalyzeController {
    @Resource
    private SpaceAnalyzeApplicationService spaceAnalyzeApplicationService;

    @Resource
    private UserApplicationService userApplicationService;

    @PostMapping("/usage")
    @Operation(summary = "分析相册或公共图库使用情况")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyze = spaceAnalyzeApplicationService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyze);
    }

    @PostMapping("/category")
    @Operation(summary = "根据分类分析使用情况，统计每类图片的数量和总大小")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(@RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> resultList = spaceAnalyzeApplicationService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

    @PostMapping("/tag")
    @Operation(summary = "根据标签分析使用情况，统计每个标签的使用次数")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> resultList = spaceAnalyzeApplicationService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

    @PostMapping("/size")
    @Operation(summary = "分析多个范围的图片体积，统计每个体积范围的图片数量")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> resultList = spaceAnalyzeApplicationService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

    @PostMapping("/user")
    @Operation(summary = "分析用户在不同时间范围内的上传情况，用于全相册分析与公共图库分析")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> resultList = spaceAnalyzeApplicationService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

    @PostMapping("/rank")
    @Operation(summary = "管理员查看相册使用量排行，分析出使用量前十名的相册、所有者")
    public BaseResponse<List<Space>> getSpaceRankAnalyze(@RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        List<Space> resultList = spaceAnalyzeApplicationService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }



}
