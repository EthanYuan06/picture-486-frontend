package com.yuluo.picture486backend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量审核图片请求
 */
@Data
public class PictureReviewByBatchRequest implements Serializable {
    /**
     * id列表
     */
    private List<Long> idList;

    /**
     * 状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
