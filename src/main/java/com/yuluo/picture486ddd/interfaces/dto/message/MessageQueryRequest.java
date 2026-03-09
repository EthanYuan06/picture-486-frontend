package com.yuluo.picture486ddd.interfaces.dto.message;

import com.yuluo.picture486ddd.infrastructure.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 消息查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MessageQueryRequest extends PageRequest implements Serializable {

    /**
     * 消息状态（0-未读 1-已读）
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
