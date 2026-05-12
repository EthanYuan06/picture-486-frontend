package com.yuluo.picture486ddd.infrastructure.mq;

import com.rabbitmq.client.Channel;
import com.yuluo.picture486ddd.domain.picture.constant.AiDescriptionMqConstants;
import com.yuluo.picture486ddd.domain.picture.dto.AiDescriptionTaskMessage;
import com.yuluo.picture486ddd.domain.picture.service.PictureDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "ai.description.mq.enabled", havingValue = "true", matchIfMissing = true)
public class AiDescriptionTaskConsumer {

    @Resource
    private PictureDomainService pictureDomainService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = AiDescriptionMqConstants.QUEUE)
    public void consume(AiDescriptionTaskMessage taskMessage, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String taskId = taskMessage.getTaskId();
        int attempt = taskMessage.getAttempt();
        try {
            pictureDomainService.processAiDescriptionTask(taskId);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            if (attempt < AiDescriptionMqConstants.MAX_RETRY) {
                AiDescriptionTaskMessage retryMessage = new AiDescriptionTaskMessage();
                retryMessage.setTaskId(taskId);
                retryMessage.setAttempt(attempt + 1);
                rabbitTemplate.convertAndSend(AiDescriptionMqConstants.EXCHANGE, AiDescriptionMqConstants.RETRY_ROUTING_KEY, retryMessage);
                channel.basicAck(deliveryTag, false);
                return;
            }
            pictureDomainService.markAiDescriptionTaskFailed(taskId, "AI处理失败，请重试");
            rabbitTemplate.convertAndSend(AiDescriptionMqConstants.EXCHANGE, AiDescriptionMqConstants.DLQ_ROUTING_KEY, taskMessage);
            channel.basicAck(deliveryTag, false);
            log.warn("AI图片简介任务进入DLQ, taskId={}", taskId, e);
        }
    }
}

