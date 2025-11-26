package com.yuluo.picture486backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.IService;

import com.yuluo.picture486backend.model.dto.space.SpaceQueryRequest;
import com.yuluo.picture486backend.model.entity.Picture;
import com.yuluo.picture486backend.model.entity.Space;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.vo.SpaceVo;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 */
public interface SpaceService extends IService<Space> {

    /**
     * 校验空间信息
     *
     * @param space 空间
     * @param add   判断是否是创建空间
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取查询条件
     *
     * @param spaceQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取空间信息
     *
     * @param space   空间信息
     * @param request 登录请求
     * @return 空间信息
     */
    SpaceVo getSpaceVo(Space space, HttpServletRequest request);

    /**
     * 获取空间分页列表
     *
     * @param spacePage 空间分页
     * @param request   登录请求
     * @return 空间列表
     */
    Page<SpaceVo> getSpaceVoPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 根据空间等级填充空间信息
     * 支持管理员自定义空间限额、数量限额
     * @param space 空间信息
     */
    void fillSpaceBySpaceLevel(Space space);














}
