package com.yuluo.picture486ddd.domain.space.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceQueryRequest;
import com.yuluo.picture486ddd.domain.space.entity.Space;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceUpdateRequest;

public interface SpaceDomainService extends IService<Space> {

    /**
     * 获取查询条件
     *
     * @param spaceQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

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
     * @param spaceUpdateRequest 相册更新信息
     */
    void updateSpace(SpaceUpdateRequest spaceUpdateRequest);

    /**
     * 根据ID获取相册信息
     * @param id 相册ID
     * @return 相册信息
     */
    Space getSpaceById(long id);

    /**
     * 分页获取相册列表
     *
     * @param spaceQueryRequest 查询条件
     * @return 分页查询结果
     */
    Page<Space> listSpaceByPage(SpaceQueryRequest spaceQueryRequest);


}
