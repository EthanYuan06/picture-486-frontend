package com.yuluo.picture486backend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486ddd.infrastructure.mapper.SysMessageMapper;
import com.yuluo.picture486backend.model.dto.message.MessageQueryRequest;
import com.yuluo.picture486backend.model.dto.message.MessageSendRequest;
import com.yuluo.picture486backend.model.entity.SysMessage;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486backend.model.vo.MessageVo;
import com.yuluo.picture486backend.service.MessageService;
import com.yuluo.picture486ddd.application.service.UserApplicationService;
import com.yuluo.picture486backend.ws.WebSocketServer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息服务实现
 */
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private SysMessageMapper sysMessageMapper;

    @Override
    public void sendMessage(Long userId, String message) {
        if (userId == null || StrUtil.isBlank(message)) {
            return;
        }

        // 1. 无论用户是否在线，先持久化消息到数据库
        SysMessage sysMessage = new SysMessage();
        sysMessage.setReceiveUserId(userId);
        sysMessage.setTitle("系统通知"); // 默认标题
        sysMessage.setContent(message);
        sysMessage.setType(0); // 0-系统消息
        sysMessage.setStatus(0); // 0-未读
        sysMessageMapper.insert(sysMessage);

        // 2. 调用 WebSocket 服务端发送消息（如果用户在线，会收到推送；如果不在线，则仅入库）
        WebSocketServer.sendMessage(userId, message);
    }

    @Override
    public void sendMessage(MessageSendRequest messageSendRequest, User loginUser) {
        // 校验参数
        if (messageSendRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = messageSendRequest.getUserId();
        String message = messageSendRequest.getMessage();
        ThrowUtils.throwIf(userId == null || StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "接收用户ID或消息内容不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 仅管理员可直接调用发送接口（例如测试或系统通知）
        if (!User.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限发送消息");
        }

        // 发送消息
        sendMessage(userId, message);
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
        QueryWrapper<SysMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiveUserId", userId);
        Integer status = messageQueryRequest.getStatus();
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        // 按时间倒序
        queryWrapper.orderByDesc("createTime");
        
        Page<SysMessage> sysMessagePage = sysMessageMapper.selectPage(new Page<>(current, size), queryWrapper);
        List<SysMessage> records = sysMessagePage.getRecords();
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
        QueryWrapper<SysMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiveUserId", loginUser.getId());
        queryWrapper.eq("status", 0); // 未读
        return sysMessageMapper.selectCount(queryWrapper);
    }

    @Override
    public boolean readMessage(Long id, User loginUser) {
        if (id == null || loginUser == null) {
            return false;
        }
        // 校验消息是否存在且属于当前用户
        SysMessage sysMessage = sysMessageMapper.selectById(id);
        if (sysMessage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "消息不存在");
        }
        if (!sysMessage.getReceiveUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该消息");
        }
        // 更新状态
        sysMessage.setStatus(1); // 已读
        return sysMessageMapper.updateById(sysMessage) > 0;
    }

    @Override
    public boolean readAllMessage(User loginUser) {
        if (loginUser == null) {
            return false;
        }
        // 更新所有未读消息为已读
        SysMessage sysMessage = new SysMessage();
        sysMessage.setStatus(1);
        QueryWrapper<SysMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiveUserId", loginUser.getId());
        queryWrapper.eq("status", 0); // 仅更新未读的
        return sysMessageMapper.update(sysMessage, queryWrapper) > 0;
    }
}
