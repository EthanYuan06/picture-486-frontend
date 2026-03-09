package com.yuluo.picture486ddd.application.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuluo.picture486ddd.domain.space.entity.Space;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.infrastructure.common.DeleteRequest;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceAddRequest;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceEditRequest;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceQueryRequest;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceUpdateRequest;
import com.yuluo.picture486ddd.interfaces.vo.space.SpaceVo;
import jakarta.servlet.http.HttpServletRequest;

public interface SpaceApplicationService extends IService<Space> {

    /**
     * 创建相册
     *
     * @param spaceAddRequest 创建相册请求
     * @param request 当前登录用户
     * @return 相册ID
     */
    long addSpace(SpaceAddRequest spaceAddRequest, HttpServletRequest request);

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

    /**
     * 更新相册
     *
     * @param spaceUpdateRequest 更新相册请求
     */
    void updateSpace(SpaceUpdateRequest spaceUpdateRequest);

    /**
     * 删除相册
     *
     * @param deleteRequest 删除相册请求
     * @param request 登录请求
     */
    void deleteSpace(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 根据ID获取相册信息
     *
     * @param id 相册ID
     * @return 相册信息
     */
    Space getSpaceById(long id);

    /**
     * 根据ID获取相册信息（脱敏）
     *
     * @param id 相册ID
     * @param request 登录请求
     * @return 相册信息
     */
    SpaceVo getSpaceVoById(long id, HttpServletRequest request);

    /**
     * 分页获取相册列表
     *
     * @param spaceQueryRequest 分页请求
     * @return 相册列表
     */
    Page<Space> listSpaceByPage(SpaceQueryRequest spaceQueryRequest);

    /**
     * 分页获取相册列表（脱敏）
     *
     * @param spaceQueryRequest 分页请求
     * @param request 登录请求
     * @return 相册列表
     */
    Page<SpaceVo> listSpaceVoByPage(SpaceQueryRequest spaceQueryRequest, HttpServletRequest request);

    /**
     * 编辑相册
     *
     * @param spaceEditRequest 编辑相册请求
     * @param request 登录请求
     */
    void editSpace(SpaceEditRequest spaceEditRequest, HttpServletRequest request);
}
