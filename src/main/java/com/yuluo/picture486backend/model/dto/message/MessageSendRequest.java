package com.yuluo.picture486backend.model.dto.message;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息发送请求
 */
@Data
public class MessageSendRequest implements Serializable {

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 消息内容
     */
    private String message;

    private static final long serialVersionUID = 1L;
}
