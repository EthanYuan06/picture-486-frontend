package com.yuluo.picture486ddd.infrastructure.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通用删除请求类
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * ID列表（用于批量操作）
     */
    private List<Long> ids;

    /**
     * 删除确认文本
     */
    private String delConfirmInfo;

    private static final long serialVersionUID = 1L;
}
