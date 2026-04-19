package com.yuluo.picture486ddd.application.service.impl;

import com.yuluo.picture486ddd.domain.picture.service.PictureDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * AI 图片简介任务异步处理器
 */
@Service
@Slf4j
public class PictureAiDescriptionTaskProcessor {

    @Resource
    private PictureDomainService pictureDomainService;

    @Async
    public void processTask(String taskId) {
        pictureDomainService.processAiDescriptionTask(taskId);
    }
}
