package com.yuluo.picture486ddd.domain.picture.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI 图片简介任务缓存对象
 */
@Data
public class AiDescriptionTask implements Serializable {
    private String taskId;

    private Long userId;

    private String status;

    private String description;

    private String errorMessage;

    private String cosObjectKey;

    private Date createTime;

    private Date updateTime;
}
