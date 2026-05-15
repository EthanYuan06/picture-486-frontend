package com.yuluo.picture486ddd.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486ddd.interfaces.dto.message.MessageQueryRequest;
import com.yuluo.picture486ddd.interfaces.dto.message.MessageSendRequest;
import com.yuluo.picture486ddd.application.service.UserApplicationService;
import com.yuluo.picture486ddd.interfaces.vo.message.MessageVo;
import com.yuluo.picture486ddd.application.service.MessageApplicationService;
import com.yuluo.picture486ddd.domain.message.service.MessageDomainService;
import com.yuluo.picture486ddd.domain.user.entity.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
