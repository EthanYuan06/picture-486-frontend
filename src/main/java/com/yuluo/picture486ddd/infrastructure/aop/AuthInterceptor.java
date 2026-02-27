package com.yuluo.picture486ddd.infrastructure.aop;

import com.yuluo.picture486ddd.infrastructure.annotation.AuthCheck;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.domain.user.valueobject.UserRoleEnum;
import com.yuluo.picture486ddd.application.service.UserApplicationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限校验 AOP
 *
 * @author yuluo
 */
@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserApplicationService userApplicationService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前登录用户
        User loginUser = userApplicationService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //某些功能无需权限，放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        //获取当前用户的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());//获取到admin/user
        //访问需要登录的功能，且用户未登录，判定为无权限，拒绝访问
        if(userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //要求必须有管理员权限，但当前用户无该权限，拒绝访问
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //通过校验，放行
        return joinPoint.proceed();
    }
}
