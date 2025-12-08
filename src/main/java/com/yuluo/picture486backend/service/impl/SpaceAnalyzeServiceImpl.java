package com.yuluo.picture486backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuluo.picture486backend.exception.BusinessException;
import com.yuluo.picture486backend.exception.ErrorCode;
import com.yuluo.picture486backend.exception.ThrowUtils;
import com.yuluo.picture486backend.model.dto.space.analysis.SpaceAnalyzeRequest;
import com.yuluo.picture486backend.model.entity.Picture;
import com.yuluo.picture486backend.model.entity.Space;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.service.SpaceAnalyzeService;
import com.yuluo.picture486backend.service.SpaceService;
import com.yuluo.picture486backend.service.UserService;
import jakarta.annotation.Resource;

public class SpaceAnalyzeServiceImpl implements SpaceAnalyzeService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    /**
     * 检查相册分析的权限
     * @param spaceAnalyzeRequest 相册分析请求
     * @param loginUser 当前登录用户
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser){
        //检查权限
        if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()){
            //全相册分析或公共图库的权限校验：仅管理员访问
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无公共图库访问权限");
        }else {
            //私有相册权限校验：仅相册创建人与管理员访问
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "相册不存在");
            spaceService.checkSpaceAuth(space, loginUser);
        }
    }

    /**
     * 填充相册分析查询条件
     * @param spaceAnalyzeRequest 相册分析请求
     * @param queryWrapper 查询条件
     */
    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper){
        //如果查询所有的相册，不拼接条件，返回
        if (spaceAnalyzeRequest.isQueryAll()){
            return;
        }
        //如果查询公共图库，拼接spaceId = null，返回
        if (spaceAnalyzeRequest.isQueryPublic()){
            queryWrapper.isNull("spaceId");
            return;
        }
        //如果查询私有相册，拼接spaceId = spaceId，返回
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceAnalyzeRequest.getSpaceId() != null){
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }





















}
