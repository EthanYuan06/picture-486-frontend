package com.yuluo.picture486ddd.application.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486backend.SpaceConstant;
import com.yuluo.picture486ddd.application.service.SpaceApplicationService;
import com.yuluo.picture486ddd.application.service.SpaceUserApplicationService;
import com.yuluo.picture486ddd.application.service.UserApplicationService;
import com.yuluo.picture486ddd.domain.space.entity.Space;
import com.yuluo.picture486ddd.domain.space.entity.SpaceUser;
import com.yuluo.picture486ddd.domain.space.service.SpaceDomainService;
import com.yuluo.picture486ddd.domain.space.valueobject.SpaceLevelEnum;
import com.yuluo.picture486ddd.domain.space.valueobject.SpaceRoleEnum;
import com.yuluo.picture486ddd.domain.space.valueobject.SpaceTypeEnum;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486ddd.infrastructure.manager.sharding.DynamicShardingManager;
import com.yuluo.picture486ddd.infrastructure.mapper.SpaceMapper;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceAddRequest;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceQueryRequest;
import com.yuluo.picture486ddd.interfaces.vo.space.SpaceVo;
import com.yuluo.picture486ddd.interfaces.vo.user.UserVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author 东山羽洛
*/
@Service
public class SpaceApplicationServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceApplicationService {

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private SpaceUserApplicationService spaceUserApplicationService;

    @Resource
    private SpaceDomainService spaceDomainService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    @Lazy
    private DynamicShardingManager dynamicShardingManager;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        //dto转实体类
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        //设置默认限额与封面
        space.fillDefaultSpace();
        this.fillSpaceBySpaceLevel(space);
        space.setSpaceCover(SpaceConstant.DEFAULT_SPACE_COVER);
        //数据校验
        space.validSpace(true);
        Long userId = loginUser.getId();
        //校验权限，不允许非管理员创建默认级别以上的相册
        if (!User.isAdmin(loginUser) && space.getSpaceLevel() != SpaceLevelEnum.COMMON.getValue()){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您没有权限创建该相册");
        }
        //设置用户ID
        space.setUserId(userId);
        String lockKey = "lock:space:create:" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        try{
            boolean locked = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "操作频繁，请稍后再试");
            }
            // 在获取锁后进行数量校验，确保校验和创建操作的原子性
            if (!User.isAdmin(loginUser) && space.getSpaceType().equals(SpaceTypeEnum.PRIVATE.getValue())) {
                // 统计普通用户的私人相册数量
                long userSpaceCount = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .eq(Space::getSpaceType, SpaceTypeEnum.PRIVATE.getValue())
                        .count();
                // 若创建则抛出异常
                if (userSpaceCount >= 5) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "仅允许创建5个私人相册");
                }
            }
            // 检查是否已存在团队相册
            if (space.getSpaceType().equals(SpaceTypeEnum.TEAM.getValue())) {
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .eq(Space::getSpaceType, SpaceTypeEnum.TEAM.getValue())
                        .exists();
                // 一个用户只能创建一个多人相册
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "只能创建一个多人相册");
            }
            //事务执行：先查后插，保证原子性
            //创建相册：管理员可无限创建相册，用户最多创建 5 个相册
            Long spaceId = transactionTemplate.execute(status -> {
                //保存相册
                boolean saved = this.save(space);
                if (!saved) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建相册失败");
                }
                //如果是多人相册，关联新增团队成员记录
                if (SpaceTypeEnum.TEAM.getValue() == spaceAddRequest.getSpaceType()) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(userId);
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    boolean result = spaceUserApplicationService.save(spaceUser);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
                }
                dynamicShardingManager.createSpacePictureTable(space);
                return space.getId();
            });
            return spaceId != null ? spaceId : 0;
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取锁被中断");
        }finally{
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }



    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        return spaceDomainService.getQueryWrapper(spaceQueryRequest);
    }

    @Override
    public SpaceVo getSpaceVo(Space space, HttpServletRequest request) {
        //space对象转换为Vo
        SpaceVo spaceVo = SpaceVo.objToVo(space);
        //关联用户查询信息
        Long userId = space.getUserId();
        if(userId != null && userId > 0){
            User user = userApplicationService.getUser(userId);
            UserVo userVo = userApplicationService.getUserVo(user);
            spaceVo.setUser(userVo);
        }
        return spaceVo;
    }

    @Override
    public Page<SpaceVo> getSpaceVoPage(Page<Space> spacePage, HttpServletRequest request) {
        //获取相册列表
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVo> spaceVoPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)){
            return spaceVoPage;
        }
        //从相册列表中获取每个相册对象，转换为Vo脱敏，再统一封装到新的列表
        List<SpaceVo> spaceVoList = spaceList.stream().map(SpaceVo::objToVo).toList();
        //关联查询用户信息（去重，使用Set集合）
        //1.从相册列表中提取所有不重复的用户ID
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        //2.批量获取用户信息，并按用户ID进行分组
        Map<Long, List<User>> userIdUserListMap = userApplicationService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        //3.填充信息
        spaceVoList.forEach(spaceVo -> {
            Long userId = spaceVo.getUserId();
            User user = null;
            if(userIdUserListMap.containsKey(userId)){
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVo.setUser(userApplicationService.getUserVo(user));
        });
        spaceVoPage.setRecords(spaceVoList);
        return spaceVoPage;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        spaceDomainService.fillSpaceBySpaceLevel(space);
    }

    @Override
    public void checkSpaceAuth(Space space, User loginUser) {
        spaceDomainService.checkSpaceAuth(space, loginUser);
    }
}




