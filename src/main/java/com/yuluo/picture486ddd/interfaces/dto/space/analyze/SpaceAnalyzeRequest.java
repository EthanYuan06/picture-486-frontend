package com.yuluo.picture486ddd.interfaces.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 相册分析请求
 * 作为各种分析请求的父类
 */
@Data
public class SpaceAnalyzeRequest implements Serializable {
    /**
     * 相册 ID
     */
    private Long spaceId;

    /**
     * 是否查询公共图库
     */
    private boolean queryPublic;

    /**
     * 全相册分析
     */
    private boolean queryAll;

    private static final long serialVersionUID = 1L;
}
