package com.yuluo.picture486backend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户重置请求
 */
@Data
public class UserResetRequest implements Serializable {
    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 校验密码
     */
    private String checkPassword;

    private static final long serialVersionUID = 873284723487234872L;
}
