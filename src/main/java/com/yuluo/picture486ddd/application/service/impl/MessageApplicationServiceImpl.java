package com.yuluo.picture486ddd.application.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486backend.model.dto.message.MessageQueryRequest;
import com.yuluo.picture486backend.model.dto.message.MessageSendRequest;
import com.yuluo.picture486ddd.application.service.UserApplicationService;
import com.yuluo.picture486ddd.domain.message.entity.SysMessage;
import com.yuluo.picture486backend.model.vo.MessageVo;
import com.yuluo.picture486ddd.application.service.MessageApplicationService;
import com.yuluo.picture486ddd.domain.message.service.MessageDomainService;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486ddd.infrastructure.websocket.WebSocketServer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息服务实现
 */
@Service
@Slf4j
public class MessageApplicationServiceImpl implements MessageApplicationService {

    @Resource
    private MessageDomainService messageDomainService;

    @Resource
    private UserApplicationService userApplicationService;

    /**
     * 给某个用户发送消息
     * @param messageSendRequest 消息发送请求
     */
    @Override
    public void sendMessage(MessageSendRequest messageSendRequest) {
        // 1.将消息存入数据库中
        String message = messageSendRequest.getMessage();
        Long userId = messageSendRequest.getUserId();
        messageDomainService.sendMessage(userId, message);
        // 2. 调用 WebSocket 服务端发送消息
        WebSocketServer.sendMessage(messageSendRequest.getUserId(), messageSendRequest.getMessage());
    }

    @Override
    public Page<MessageVo> listMessageVoByPage(MessageQueryRequest messageQueryRequest, HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        return messageDomainService.listMessageVoByPage(messageQueryRequest, loginUser);
    }

    @Override
    public long getUnreadMessageCount(HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        return messageDomainService.getUnreadMessageCount(loginUser);
    }

    @Override
    public boolean readMessage(Long id, HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        return messageDomainService.readMessage(id, loginUser);
    }

    @Override
    public boolean readAllMessage(HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        return messageDomainService.readAllMessage(loginUser);
    }
}
