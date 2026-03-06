package com.yuluo.picture486ddd.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486backend.model.dto.message.MessageQueryRequest;
import com.yuluo.picture486backend.model.dto.message.MessageSendRequest;
import com.yuluo.picture486backend.model.vo.MessageVo;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 消息服务
 */
public interface MessageApplicationService {

    /**
     * 发送消息
     *
     * @param messageSendRequest 消息发送请求
     */
    void sendMessage(MessageSendRequest messageSendRequest);

    /**
     * 分页获取消息列表
     *
     * @param messageQueryRequest 消息查询请求
     * @param request           当前登录用户
     * @return 消息列表
     */
    Page<MessageVo> listMessageVoByPage(MessageQueryRequest messageQueryRequest, HttpServletRequest request);

    /**
     * 获取未读消息数量
     *
     * @param request 当前登录用户
     * @return 未读消息数量
     */
    long getUnreadMessageCount(HttpServletRequest request);

    /**
     * 标记消息为已读
     *
     * @param id        消息ID
     * @param request 当前登录用户
     * @return 是否成功
     */
    boolean readMessage(Long id, HttpServletRequest request);

    /**
     * 标记所有消息为已读
     *
     * @param request 当前登录用户
     * @return 是否成功
     */
    boolean readAllMessage(HttpServletRequest request);
}
