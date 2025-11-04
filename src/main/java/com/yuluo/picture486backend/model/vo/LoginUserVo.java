package com.yuluo.picture486backend.model.vo;


import com.baomidou.mybatisplus.annotation.TableField;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录用户视图，返回脱敏用户信息对象
 * @ TableName user
 */
@Data
public class LoginUserVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
