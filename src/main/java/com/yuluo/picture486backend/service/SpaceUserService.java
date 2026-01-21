package com.yuluo.picture486backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuluo.picture486backend.model.dto.space_user.SpaceUserAddRequest;
import com.yuluo.picture486backend.model.dto.space_user.SpaceUserQueryRequest;
import com.yuluo.picture486backend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuluo.picture486backend.model.vo.space_user.SpaceUserVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author 东山羽洛
*/
public interface SpaceUserService extends IService<SpaceUser> {
    /**
     * 添加相册成员
     *
     * @param spaceUserAddRequest 添加相册成员请求
     * @return 添加的成员 id
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验相册成员
     *
     * @param spaceUser 待校验的相册成员
     * @param add       是否为创建校验
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 相册成员查询
     *
     * @param spaceUserQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取相册成员封装类
     *
     * @param spaceUser 待转换的相册成员
     * @param request   请求
     * @return 相册成员封装类
     */
    SpaceUserVo getSpaceUserVo(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 查询封装类列表
     *
     * @param spaceUserList 待转换的相册成员列表
     * @return 列表相册成员封装类
     */
    List<SpaceUserVo> getSpaceUserVoList(List<SpaceUser> spaceUserList);






















}
