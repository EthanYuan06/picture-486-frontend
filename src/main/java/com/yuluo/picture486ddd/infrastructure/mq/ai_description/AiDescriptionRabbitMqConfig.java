package com.yuluo.picture486ddd.infrastructure.mq.ai_description;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * AI图片简介 MQ 配置类
 */
@Configuration
@ConditionalOnProperty(name = "ai.description.mq.enabled", havingValue = "true", matchIfMissing = true)
public class AiDescriptionRabbitMqConfig {

    @Bean
    public DirectExchange aiDescriptionExchange() {
        return new DirectExchange(AiDescriptionMqConstants.EXCHANGE, true, false);
    }

    @Bean
    public Queue aiDescriptionQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", AiDescriptionMqConstants.EXCHANGE);
        args.put("x-dead-letter-routing-key", AiDescriptionMqConstants.DLQ_ROUTING_KEY);
        return new Queue(AiDescriptionMqConstants.QUEUE, true, false, false, args);
    }

    @Bean
    public Queue aiDescriptionRetryQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", AiDescriptionMqConstants.RETRY_DELAY_MS);
        args.put("x-dead-letter-exchange", AiDescriptionMqConstants.EXCHANGE);
        args.put("x-dead-letter-routing-key", AiDescriptionMqConstants.ROUTING_KEY);
        return new Queue(AiDescriptionMqConstants.RETRY_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue aiDescriptionDlq() {
        return new Queue(AiDescriptionMqConstants.DLQ, true);
    }

    @Bean
    public Binding aiDescriptionBinding(Queue aiDescriptionQueue, DirectExchange aiDescriptionExchange) {
        return BindingBuilder.bind(aiDescriptionQueue).to(aiDescriptionExchange).with(AiDescriptionMqConstants.ROUTING_KEY);
    }

    @Bean
    public Binding aiDescriptionRetryBinding(Queue aiDescriptionRetryQueue, DirectExchange aiDescriptionExchange) {
        return BindingBuilder.bind(aiDescriptionRetryQueue).to(aiDescriptionExchange).with(AiDescriptionMqConstants.RETRY_ROUTING_KEY);
    }

    @Bean
    public Binding aiDescriptionDlqBinding(Queue aiDescriptionDlq, DirectExchange aiDescriptionExchange) {
        return BindingBuilder.bind(aiDescriptionDlq).to(aiDescriptionExchange).with(AiDescriptionMqConstants.DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
