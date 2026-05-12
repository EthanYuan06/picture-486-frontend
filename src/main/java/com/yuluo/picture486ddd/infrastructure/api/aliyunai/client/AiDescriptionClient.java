package com.yuluo.picture486ddd.infrastructure.api.aliyunai.client;

public interface AiDescriptionClient {
    /**
     * 生成图片简介
     */
    String generate(String base64Image, String mimeType) throws Exception;
}

