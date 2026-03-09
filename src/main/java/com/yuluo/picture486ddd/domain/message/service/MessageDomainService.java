package com.yuluo.picture486ddd.domain.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486ddd.interfaces.dto.message.MessageQueryRequest;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.interfaces.vo.message.MessageVo;

/**
 * 消息服务
 */
public interface MessageDomainService {

    /**
     * 给指定用户发送消息
     * @param userId 用户ID
     * @param message 消息内容
     */
    void sendMessage(Long userId, String message);

    /**
     * 分页获取消息列表
     *
     * @param messageQueryRequest 消息查询请求
     * @param loginUser           当前登录用户
     * @return 消息列表
     */
    Page<MessageVo> listMessageVoByPage(MessageQueryRequest messageQueryRequest, User loginUser);

    /**
     * 获取未读消息数量
     *
     * @param loginUser 当前登录用户
     * @return 未读消息数量
     */
    long getUnreadMessageCount(User loginUser);

    /**
     * 标记消息为已读
     *
     * @param id        消息ID
     * @param loginUser 当前登录用户
     * @return 是否成功
     */
    boolean readMessage(Long id, User loginUser);

    /**
     * 标记所有消息为已读
     *
     * @param loginUser 当前登录用户
     * @return 是否成功
     */
    boolean readAllMessage(User loginUser);
}
