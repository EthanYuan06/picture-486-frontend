package com.yuluo.picture486ddd.domain.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486ddd.domain.message.repository.MessageRepository;
import com.yuluo.picture486ddd.domain.message.service.MessageDomainService;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486ddd.infrastructure.mapper.MessageMapper;
import com.yuluo.picture486ddd.interfaces.dto.message.MessageQueryRequest;
import com.yuluo.picture486ddd.domain.message.entity.Message;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.interfaces.vo.message.MessageVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息服务实现
 */
@Service
@Slf4j
public class MessageDomainServiceImpl implements MessageDomainService {
    @Resource
    private MessageRepository messageRepository;
    @Resource
    private MessageMapper messageMapper;

    @Override
    public void sendMessage(Long userId, String message) {
        // 无论用户是否在线，先持久化消息到数据库
        Message sysMessage = new Message();
        sysMessage.setReceiveUserId(userId);
        sysMessage.setTitle("系统通知"); // 默认标题
        sysMessage.setContent(message);
        sysMessage.setType(0); // 0-系统消息
        sysMessage.setStatus(0); // 0-未读
        messageRepository.insert(sysMessage);
    }

    @Override
    public Page<MessageVo> listMessageVoByPage(MessageQueryRequest messageQueryRequest, User loginUser) {
        if (messageQueryRequest == null) {
            return new Page<>();
        }
        long current = messageQueryRequest.getCurrent();
        long size = messageQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询当前登录用户的消息
        Long userId = loginUser.getId();
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiveUserId", userId);
        Integer status = messageQueryRequest.getStatus();
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        // 按时间倒序
        queryWrapper.orderByDesc("createTime");
        
        Page<Message> sysMessagePage = messageRepository.selectPage(new Page<>(current, size), queryWrapper);
        List<Message> records = sysMessagePage.getRecords();
        if (records == null || records.isEmpty()) {
            return new Page<>(current, size, 0);
        }
        // 转换为VO
        List<MessageVo> messageVoList = records.stream().map(MessageVo::objToVo).toList();
        Page<MessageVo> messageVoPage = new Page<>(sysMessagePage.getCurrent(), sysMessagePage.getSize(), sysMessagePage.getTotal());
        messageVoPage.setRecords(messageVoList);
        return messageVoPage;
    }

    @Override
    public long getUnreadMessageCount(User loginUser) {
        if (loginUser == null) {
            return 0;
        }
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiveUserId", loginUser.getId());
        queryWrapper.eq("status", 0); // 未读
        return messageRepository.selectCount(queryWrapper);
    }

    @Override
    public boolean readMessage(Long id, User loginUser) {
        if (id == null || loginUser == null) {
            return false;
        }
        // 校验消息是否存在且属于当前用户
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "消息不存在");
        }
        if (!message.getReceiveUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该消息");
        }
        // 更新状态
        message.setStatus(1); // 已读
        return messageRepository.updateById(message) > 0;
    }

    @Override
    public boolean readAllMessage(User loginUser) {
        if (loginUser == null) {
            return false;
        }
        // 更新所有未读消息为已读
        Message message = new Message();
        message.setStatus(1);
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiveUserId", loginUser.getId());
        queryWrapper.eq("status", 0); // 仅更新未读的
        return messageRepository.update(message, queryWrapper) > 0;
    }
}
