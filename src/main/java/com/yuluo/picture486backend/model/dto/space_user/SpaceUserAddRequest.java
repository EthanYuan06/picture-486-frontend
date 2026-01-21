package com.yuluo.picture486backend.model.dto.space_user;

import lombok.Data;

import java.io.Serializable;

/**
 * 相册成员创建请求
 */
@Data
public class SpaceUserAddRequest implements Serializable {

    /**
     * 相册 ID
     */
    private Long spaceId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 相册角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}
