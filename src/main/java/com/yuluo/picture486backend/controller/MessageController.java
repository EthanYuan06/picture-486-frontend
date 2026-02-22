package com.yuluo.picture486backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486backend.common.BaseResponse;
import com.yuluo.picture486backend.common.ResultUtils;
import com.yuluo.picture486backend.model.dto.message.MessageQueryRequest;
import com.yuluo.picture486backend.model.dto.message.MessageSendRequest;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.vo.MessageVo;
import com.yuluo.picture486backend.service.MessageService;
import com.yuluo.picture486backend.service.UserService;
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
    private MessageService messageService;

    @Resource
    private UserService userService;

    /**
     * 发送消息（仅管理员）
     */
    @PostMapping("/send")
    public BaseResponse<Boolean> sendMessage(@RequestBody MessageSendRequest messageSendRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        messageService.sendMessage(messageSendRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取消息列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<MessageVo>> listMessageVoByPage(@RequestBody MessageQueryRequest messageQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<MessageVo> messageVoPage = messageService.listMessageVoByPage(messageQueryRequest, loginUser);
        return ResultUtils.success(messageVoPage);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public BaseResponse<Long> getUnreadMessageCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long count = messageService.getUnreadMessageCount(loginUser);
        return ResultUtils.success(count);
    }

    /**
     * 标记消息为已读
     */
    @GetMapping("/read")
    public BaseResponse<Boolean> readMessage(Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = messageService.readMessage(id, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 标记所有消息为已读
     */
    @GetMapping("/read/all")
    public BaseResponse<Boolean> readAllMessage(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = messageService.readAllMessage(loginUser);
        return ResultUtils.success(result);
    }
}
