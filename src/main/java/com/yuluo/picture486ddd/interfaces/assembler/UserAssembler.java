package com.yuluo.picture486ddd.interfaces.assembler;

import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.interfaces.dto.user.UserAddRequest;
import com.yuluo.picture486ddd.interfaces.dto.user.UserRegisterRequest;
import com.yuluo.picture486ddd.interfaces.dto.user.UserUpdateRequest;
import org.springframework.beans.BeanUtils;

/**
 * 用户对象转换
 */
public class UserAssembler {

    public static User toUserEntity(UserRegisterRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user);
        return user;
    }

    public static User toUserEntity(UserAddRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user);
        return user;
    }

    public static User toUserEntity(UserUpdateRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user);
        return user;
    }
}
