package com.yuluo.picture486backend.service;

import com.yuluo.picture486backend.model.dto.space.analyze.*;
import com.yuluo.picture486backend.model.entity.Space;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.vo.space.analyze.*;

import java.util.List;

public interface SpaceAnalyzeService {
    /**
     * 获取相册使用情况分析
     *
     * @param spaceUsageAnalyzeRequest 相册分析请求
     * @param loginUser                当前登录用户
     * @return 相册`使用情况分析结果
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);
    
    /**
     * 获取相册图片分类分析
     *
     * @param spaceCategoryAnalyzeRequest 相册分析请求
     * @param loginUser                   当前登录用户
     * @return 相册图片分类分析结果
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 获取相册图片标签分析
     *
     * @param spaceTagAnalyzeRequest 相册分析请求
     * @param loginUser              当前登录用户
     * @return 相册图片标签分析结果
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取相册图片大小分析
     *
     * @param spaceSizeAnalyzeRequests 相册分析请求
     * @param loginUser                当前登录用户
     * @return 相册图片大小分析结果
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequests, User loginUser);

    /**
     * 获取相册用户分析
     *
     * @param spaceUserAnalyzeRequest 相册分析请求
     * @param loginUser               当前登录用户
     * @return 相册用户分析结果
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 获取相册排行分析
     *
     * @param spaceRankAnalyzeRequest 相册分析请求
     * @param loginUser               当前登录用户
     * @return 相册排行分析结果
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);








}
