package com.yuluo.picture486backend.model.dto.space_user;

import lombok.Data;

import java.io.Serializable;

/**
 * 相册成员编辑请求，设置空间成员的角色
 */
@Data
public class SpaceUserEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 相册角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}
