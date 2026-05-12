package com.yuluo.picture486ddd.domain.picture.constant;

/**
 * RabbitMQ 常量
 */
public interface AiDescriptionMqConstants {
    String EXCHANGE = "ai.description.ex";
    String QUEUE = "ai.description.q";
    String RETRY_QUEUE = "ai.description.retry.q";
    String DLQ = "ai.description.dlq";
    String ROUTING_KEY = "ai.description";
    String RETRY_ROUTING_KEY = "ai.description.retry";
    String DLQ_ROUTING_KEY = "ai.description.dlq";
    int MAX_RETRY = 3;
    int RETRY_DELAY_MS = 10_000;
}

