package com.yuluo.picture486ddd.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.domain.user.repository.UserRepository;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements UserRepository {
    /**
     * 检测账号是否已存在
     *
     * @param userAccount 账号
     * */
    @Override
    public void existAccount(String userAccount) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已被注册");
        }
    }
}
