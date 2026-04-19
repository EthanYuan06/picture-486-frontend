package com.yuluo.picture486ddd.interfaces.vo.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 图片简介任务返回对象
 */
@Data
public class AiDescriptionTaskVo implements Serializable {
    private String taskId;

    private String status;

    private String description;

    private String errorMessage;
}
