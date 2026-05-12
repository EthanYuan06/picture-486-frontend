package com.yuluo.picture486ddd.infrastructure.api.aliyunai.client;

import com.yuluo.picture486ddd.infrastructure.api.aliyunai.model.AiDescription;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AI 生成简介客户端
 */
@Component
@ConditionalOnProperty(name = "ai.description.provider", havingValue = "dashscope", matchIfMissing = true)
public class DashScopeAiDescriptionClient implements AiDescriptionClient {

    @Override
    public String generate(String base64Image, String mimeType) throws Exception {
        return AiDescription.callWithBase64(base64Image, mimeType);
    }
}

