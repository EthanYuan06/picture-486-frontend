package com.yuluo.picture486ddd.infrastructure.manager.auth;

import com.yuluo.picture486ddd.domain.picture.entity.Picture;
import com.yuluo.picture486backend.model.entity.Space;
import com.yuluo.picture486backend.model.entity.SpaceUser;
import lombok.Data;

/**
 * SpaceUserAuthContext
 * 表示用户在特定相册内的授权上下文，包括关联的图片、相册和用户信息。
 */
@Data
public class SpaceUserAuthContext {

    /**
     * 临时参数，不同请求对应的 id 可能不同
     */
    private Long id;

    /**
     * 图片 ID
     */
    private Long pictureId;

    /**
     * 相册 ID
     */
    private Long spaceId;

    /**
     * 相册用户 ID
     */
    private Long spaceUserId;

    /**
     * 图片信息
     */
    private Picture picture;

    /**
     * 相册信息
     */
    private Space space;

    /**
     * 相册用户信息
     */
    private SpaceUser spaceUser;
}
