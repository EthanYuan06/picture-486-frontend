package com.yuluo.picture486backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
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
}