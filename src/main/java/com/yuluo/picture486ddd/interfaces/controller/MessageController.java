package com.yuluo.picture486ddd.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486ddd.application.service.MessageApplicationService;
import com.yuluo.picture486ddd.infrastructure.annotation.AuthCheck;
import com.yuluo.picture486ddd.infrastructure.common.BaseResponse;
import com.yuluo.picture486ddd.infrastructure.common.ResultUtils;
import com.yuluo.picture486backend.model.dto.message.MessageQueryRequest;
import com.yuluo.picture486backend.model.dto.message.MessageSendRequest;
import com.yuluo.picture486backend.model.vo.MessageVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 消息接口
 */
@RestController
@RequestMapping("/message")
public class MessageController {
    @Resource
    private MessageApplicationService messageApplicationService;

    /**
     * 给指定用户发送消息（仅管理员）
     */
    @PostMapping("/send")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> sendMessage(@RequestBody MessageSendRequest messageSendRequest) {
        messageApplicationService.sendMessage(messageSendRequest);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取消息列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<MessageVo>> listMessageVoByPage(
            @RequestBody MessageQueryRequest messageQueryRequest, HttpServletRequest request) {
        return ResultUtils.success(messageApplicationService.listMessageVoByPage(messageQueryRequest, request));
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public BaseResponse<Long> getUnreadMessageCount(HttpServletRequest request) {
        return ResultUtils.success(messageApplicationService.getUnreadMessageCount(request));
    }

    /**
     * 标记消息为已读
     */
    @GetMapping("/read")
    public BaseResponse<Boolean> readMessage(Long id, HttpServletRequest request) {
        return ResultUtils.success(messageApplicationService.readMessage(id, request));
    }

    /**
     * 标记所有消息为已读
     */
    @GetMapping("/read/all")
    public BaseResponse<Boolean> readAllMessage(HttpServletRequest request) {
        return ResultUtils.success(messageApplicationService.readAllMessage(request));
    }
}
