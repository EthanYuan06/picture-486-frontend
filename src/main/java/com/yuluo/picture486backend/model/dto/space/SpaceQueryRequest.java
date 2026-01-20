package com.yuluo.picture486backend.model.dto.space;

import com.yuluo.picture486backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 相册查询请求类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {

    /**
     * 相册 id
     */
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 相册名称
     */
    private String spaceName;

    /**
     * 相册级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 相册类型：0-私有 1-多人
     */
    private Integer spaceType;


    private static final long serialVersionUID = 1L;
}
