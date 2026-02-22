package com.yuluo.picture486backend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.yuluo.picture486backend.model.entity.SysMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息 VO
 */
@Data
public class MessageVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 接收消息的用户id
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

    /**
     * 消息类型（0-系统消息）
     */
    private Integer type;

    /**
     * 状态（0-未读 1-已读）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    /**
     * 包装类转对象
     *
     * @param sysMessage
     * @return
     */
    public static MessageVo objToVo(SysMessage sysMessage) {
        if (sysMessage == null) {
            return null;
        }
        MessageVo messageVo = new MessageVo();
        BeanUtil.copyProperties(sysMessage, messageVo);
        return messageVo;
    }
}
