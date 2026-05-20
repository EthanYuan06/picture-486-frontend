package com.yuluo.picture486ddd.interfaces.dto.message;

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
    private Long receiveUserId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}
