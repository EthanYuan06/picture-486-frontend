-- auto-generated definition
create table sys_message
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    receiveUserId bigint                              not null comment '接收用户ID',
    title         varchar(255)                        not null comment '消息标题',
    content       varchar(1000)                       null comment '消息内容',
    type          tinyint   default 0                 not null comment '消息类型（0-系统通知 1-用户消息等）',
    status        tinyint   default 0                 not null comment '消息状态（0-未读 1-已读 2-已删除）',
    createTime    timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime    timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint   default 0                 not null comment '是否删除（0-未删除 1-已删除）'
)
    comment '系统消息表' collate = utf8mb4_unicode_ci;

create index idx_create_time
    on sys_message (createTime);

create index idx_receive_user_id
    on sys_message (receiveUserId);

create index idx_status
    on sys_message (status);

