package com.yuluo.picture486ddd.domain.space.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.yuluo.picture486ddd.domain.constant.SpaceConstant;
import com.yuluo.picture486ddd.domain.space.valueobject.SpaceLevelEnum;
import com.yuluo.picture486ddd.domain.space.valueobject.SpaceTypeEnum;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import lombok.Data;

/**
 * 相册
 */
@TableName(value ="space")
@Data
public class Space implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 相册类型：0-私有 1-多人
     */
    private Integer spaceType;

    /**
     * 相册封面
     */
    private String spaceCover;

    /**
     * 相册级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

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
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public void fillDefaultSpace() {
        //设置默认相册名称、等级以及相册类型
        if (StrUtil.isBlank(this.getSpaceName())) {
            this.setSpaceName(SpaceConstant.DEFAULT_SPACE_NAME);
        }
        if (this.getSpaceLevel() == null) {
            this.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (this.getSpaceType() == null) {
            this.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
    }

    public void validSpace(boolean isAdd) {
        //从对象中取值
        String spaceName = this.getSpaceName();
        Integer spaceLevel = this.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = this.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        //判断是否是创建相册
        if (isAdd) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册名不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册等级不能为空");
            }
            if (spaceType == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册类型不能为空");
            }
        }
        //修改数据时，更改相册级别时的判定
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册等级不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 24) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册名称过长");
        }
        if (spaceType != null && spaceTypeEnum == null) {
            //随意输入一个数字绕过spaceType != null也不能绕过spaceTypeEnum == null的结果
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册类型不存在");
        }
    }
}