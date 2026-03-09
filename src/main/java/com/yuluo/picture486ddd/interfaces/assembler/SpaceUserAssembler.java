package com.yuluo.picture486ddd.interfaces.assembler;

import com.yuluo.picture486ddd.domain.space.entity.SpaceUser;
import com.yuluo.picture486ddd.interfaces.dto.space_user.SpaceUserAddRequest;
import com.yuluo.picture486ddd.interfaces.dto.space_user.SpaceUserEditRequest;
import com.yuluo.picture486ddd.interfaces.dto.space_user.SpaceUserQueryRequest;
import org.springframework.beans.BeanUtils;

/**
 * 相册成员接口转换器
 */
public class SpaceUserAssembler {

    public static SpaceUser toSpaceUserEntity(SpaceUserAddRequest request) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(request, spaceUser);
        return spaceUser;
    }

    public static SpaceUser toSpaceUserEntity(SpaceUserEditRequest request) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(request, spaceUser);
        return spaceUser;
    }

    public static SpaceUser toSpaceUserEntity(SpaceUserQueryRequest request) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(request, spaceUser);
        return spaceUser;
    }
}
