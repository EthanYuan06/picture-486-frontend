package com.yuluo.picture486ddd.domain.space.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuluo.picture486backend.model.dto.space_user.SpaceUserAddRequest;
import com.yuluo.picture486backend.model.dto.space_user.SpaceUserQueryRequest;
import com.yuluo.picture486ddd.domain.space.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 东山羽洛
*/
public interface SpaceUserDomainService extends IService<SpaceUser> {
    /**
     * 相册成员查询
     *
     * @param spaceUserQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);


}
