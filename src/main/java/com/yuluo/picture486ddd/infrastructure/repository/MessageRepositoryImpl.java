package com.yuluo.picture486ddd.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486ddd.domain.message.entity.Message;
import com.yuluo.picture486ddd.domain.message.repository.MessageRepository;
import com.yuluo.picture486ddd.infrastructure.mapper.MessageMapper;
import org.springframework.stereotype.Service;

@Service
public class MessageRepositoryImpl extends ServiceImpl<MessageMapper, Message> implements MessageRepository {
}
