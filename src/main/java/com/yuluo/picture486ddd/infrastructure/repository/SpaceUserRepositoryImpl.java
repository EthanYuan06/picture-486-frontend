package com.yuluo.picture486ddd.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486ddd.domain.space.entity.SpaceUser;
import com.yuluo.picture486ddd.domain.space.repository.SpaceUserRepository;
import com.yuluo.picture486ddd.infrastructure.mapper.SpaceUserMapper;
import org.springframework.stereotype.Service;

@Service
public class SpaceUserRepositoryImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserRepository {
}
