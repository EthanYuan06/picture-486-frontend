package com.yuluo.picture486ddd.domain.picture.entity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import lombok.Data;

/**
 * 图片
 * @TableName picture
 */
@TableName(value ="picture")
@Data
public class Picture implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
     * 状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 id
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public static void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        //从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        //修改数据时，id不能为空，有参数则校验
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        if(StrUtil.isNotBlank(url)){
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url过长");
        }
        if(StrUtil.isNotBlank(introduction)){
            ThrowUtils.throwIf(introduction.length() > 1024, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    /**
     * 批量重命名
     * @param pictureList 图片列表
     * @param nameRule 名称规则 图片名_{序号}
     */
    public static void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        //校验参数
        if (StrUtil.isBlank(nameRule) || CollUtil.isEmpty(pictureList)){
            return;
        }
        //批量重命名
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String newName = nameRule.replace("{序号}", String.valueOf(count));
                picture.setName(newName);
                count++;
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }
}