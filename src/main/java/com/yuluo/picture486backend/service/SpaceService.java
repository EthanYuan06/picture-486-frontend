package com.yuluo.picture486backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuluo.picture486backend.model.dto.space.SpaceAddRequest;
import com.yuluo.picture486backend.model.dto.space.SpaceQueryRequest;
import com.yuluo.picture486backend.model.entity.Space;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.vo.SpaceVo;
import jakarta.servlet.http.HttpServletRequest;

public interface SpaceService extends IService<Space> {

    /**
     * 创建相册
     *
     * @param spaceAddRequest 创建相册请求
     * @param loginUser 当前登录用户
     * @return 相册ID
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验相册信息
     *
     * @param space 相册
     * @param isAdd   判断是否是创建相册
     */
    void validSpace(Space space, boolean isAdd);

    /**
     * 获取查询条件
     *
     * @param spaceQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取相册信息
     *
     * @param space   相册信息
     * @param request 登录请求
     * @return 相册信息
     */
    SpaceVo getSpaceVo(Space space, HttpServletRequest request);

    /**
     * 获取相册分页列表
     *
     * @param spacePage 相册分页
     * @param request   登录请求
     * @return 相册列表
     */
    Page<SpaceVo> getSpaceVoPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 根据相册等级填充相册信息
     * 支持管理员自定义相册限额、数量限额
     * @param space 相册信息
     */
    void fillSpaceBySpaceLevel(Space space);


    /**
     * 检查相册访问权限
     *
     * @param space   相册信息
     * @param loginUser 当前登录用户
     */
    void checkSpaceAuth(Space space, User loginUser);
}
