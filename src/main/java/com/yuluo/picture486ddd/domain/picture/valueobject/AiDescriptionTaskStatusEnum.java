package com.yuluo.picture486ddd.domain.picture.valueobject;

import lombok.Getter;

/**
 * AI 图片简介任务状态
 */
@Getter
public enum AiDescriptionTaskStatusEnum {
    PROCESSING("processing"),
    SUCCESS("success"),
    FAILED("failed");

    private final String value;

    AiDescriptionTaskStatusEnum(String value) {
        this.value = value;
    }
}
