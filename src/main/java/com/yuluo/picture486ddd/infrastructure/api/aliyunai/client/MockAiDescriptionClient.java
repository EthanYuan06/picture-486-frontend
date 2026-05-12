package com.yuluo.picture486ddd.infrastructure.api.aliyunai.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@ConditionalOnProperty(name = "ai.description.provider", havingValue = "mock")
public class MockAiDescriptionClient implements AiDescriptionClient {

    @Value("${ai.description.mock.minDelayMs:200}")
    private int minDelayMs;

    @Value("${ai.description.mock.maxDelayMs:800}")
    private int maxDelayMs;

    @Value("${ai.description.mock.failRate:0.05}")
    private double failRate;

    @Override
    public String generate(String base64Image, String mimeType) throws Exception {
        int delay = ThreadLocalRandom.current().nextInt(minDelayMs, Math.max(minDelayMs + 1, maxDelayMs + 1));
        Thread.sleep(delay);
        if (ThreadLocalRandom.current().nextDouble() < failRate) {
            throw new IllegalStateException("Mock AI failure");
        }
        String safeMimeType = (mimeType == null || mimeType.isBlank()) ? "image/*" : mimeType;
        return "（Mock）已生成图片简介，mimeType=" + safeMimeType + "，delayMs=" + delay;
    }
}

