package com.yuluo.picture486backend.model.vo;

import com.yuluo.picture486backend.model.entity.Space;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class SpaceVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 相册名称
     */
    private String spaceName;
    
    /**
     * 相册简介
     */
    private String spaceDesc;

    /**
     * 相册封面
     */
    private String spaceCover;

    /**
     * 相册级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 相册类型：0-私有 1-多人
     */
    private Integer spaceType;

    /**
     * 相册图片的最大总大小
     */
    private Long maxSize;

    /**
     * 相册图片的最大数量
     */
    private Long maxCount;

    /**
     * 当前相册下图片的总大小
     */
    private Long totalSize;

    /**
     * 当前相册下的图片数量
     */
    private Long totalCount;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVo user;

    private static final long serialVersionUID = 1L;

    /**
     * 封装类转对象
     */
    public static Space voToObj(SpaceVo spaceVO) {
        if (spaceVO == null) {
            return null;
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceVO, space);
        return space;
    }

    /**
     * 对象转封装类
     */
    public static SpaceVo objToVo(Space space) {
        if (space == null) {
            return null;
        }
        SpaceVo spaceVO = new SpaceVo();
        BeanUtils.copyProperties(space, spaceVO);
        return spaceVO;
    }
}
