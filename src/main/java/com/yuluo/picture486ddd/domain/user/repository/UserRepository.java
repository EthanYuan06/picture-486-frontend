package com.yuluo.picture486ddd.domain.user.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuluo.picture486ddd.domain.user.entity.User;

public interface UserRepository extends IService<User> {
 void existAccount(String userAccount);
}
