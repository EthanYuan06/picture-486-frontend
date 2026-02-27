package com.yuluo.picture486ddd.interfaces.dto.picture;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yuluo.picture486ddd.infrastructure.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {
  
    /**  
     * id  
     */  
    private Long id;  
  
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
     * 标签  
     */  
    private List<String> tags;
  
    /**  
     * 文件体积  
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
     * 图片比例  
     */  
    private Double picScale;  
  
    /**  
     * 图片格式  
     */  
    private String picFormat;  
  
    /**  
     * 搜索词（同时搜名称、简介等）  
     */  
    private String searchText;  
  
    /**  
     * 用户 id  
     */  
    private Long userId;

    /**
     * 相册 id
     */
    private Long spaceId;

    /**
     * 是否只查询相册 id 为空的图片
     */
    private boolean nullSpaceId;

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
     * 创建时间开始
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTimeStart;

    /**
     * 创建时间结束
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTimeEnd;

    /**
     * 编辑时间开始
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date editTimeStart;

    /**
     * 编辑时间结束
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date editTimeEnd;

    private static final long serialVersionUID = 1L;  
}
