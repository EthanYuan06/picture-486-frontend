-- 系统消息表
CREATE TABLE sys_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    receiveUserId BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(255) NOT NULL COMMENT '消息标题',
    content VARCHAR(1000) COMMENT '消息内容',
    type TINYINT NOT NULL DEFAULT 0 COMMENT '消息类型（0-系统通知 1-用户消息等）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '消息状态（0-未读 1-已读 2-已删除）',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除 1-已删除）',
    PRIMARY KEY (id),
    INDEX idx_receive_user_id (receiveUserId),
    INDEX idx_create_time (createTime),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统消息表';