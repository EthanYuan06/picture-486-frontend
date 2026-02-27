package com.yuluo.picture486ddd.domain.user.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yuluo.picture486ddd.domain.user.valueobject.UserRoleEnum;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 用户实体类
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 密码
     */
    private String userPassword;

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

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 校验用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 密码校验
     */
    public static void validUserRegister(String userAccount, String userEmail, String userPassword, String checkPassword){
        //校验参数与账号密码
        if (StringUtils.isAnyBlank(userAccount, userEmail, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        validKey(userAccount, userPassword, checkPassword);

        // 校验邮箱格式
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@((?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,})$";
        Pattern emailRegex = Pattern.compile(emailPattern);
        Matcher emailMatcher = emailRegex.matcher(userEmail);
        if (!emailMatcher.matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入有效的邮箱地址");
        }

        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }
    }

    /**
     * 校验用户输入的信息
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     */
    public static void validKey(String userAccount, String userPassword, String checkPassword) {
        if (userAccount.length() < 4 || userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名长度应为4-20个字符");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于8个字符");
        }

        // 账户必须包含字母和数字，且不能以数字开头
        if (!Character.isLetter(userAccount.charAt(0))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能以数字开头");
        }
        if (!userAccount.matches(".*[a-zA-Z]+.*") || !userAccount.matches(".*\\d+.*")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号必须同时包含字母和数字");
        }

        // 密码必须包含大小写字母和数字
        if (!userPassword.matches(".*[a-z]+.*") || !userPassword.matches(".*[A-Z]+.*") || !userPassword.matches(".*\\d+.*")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码必须包含大小写字母和数字");
        }

        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
    }

    /**
     * 校验用户登录
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     */
    public static void validUserLogin(String userAccount, String userPassword) {
        //1.校验账号密码
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
    }

    public static boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }
}