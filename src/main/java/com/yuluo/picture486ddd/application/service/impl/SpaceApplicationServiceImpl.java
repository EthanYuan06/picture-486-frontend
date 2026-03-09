package com.yuluo.picture486ddd.application.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486backend.SpaceConstant;
import com.yuluo.picture486ddd.application.service.PictureApplicationService;
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
import com.yuluo.picture486ddd.infrastructure.common.DeleteRequest;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486ddd.infrastructure.manager.auth.SpaceUserAuthManager;
import com.yuluo.picture486ddd.infrastructure.manager.sharding.DynamicShardingManager;
import com.yuluo.picture486ddd.infrastructure.mapper.SpaceMapper;
import com.yuluo.picture486ddd.interfaces.assembler.SpaceAssembler;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceAddRequest;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceEditRequest;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceQueryRequest;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceUpdateRequest;
import com.yuluo.picture486ddd.interfaces.vo.space.SpaceVo;
import com.yuluo.picture486ddd.interfaces.vo.user.UserVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
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
    private PictureApplicationService pictureApplicationService;

    @Resource
    private SpaceUserApplicationService spaceUserApplicationService;

    @Resource
    private SpaceDomainService spaceDomainService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Resource
    @Lazy
    private DynamicShardingManager dynamicShardingManager;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        if (spaceAddRequest == null || request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userApplicationService.getLoginUser(request);
        //dto转实体类
        Space space = SpaceAssembler.toSpaceEntity(spaceAddRequest);
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

    @Override
    public void updateSpace(SpaceUpdateRequest spaceUpdateRequest) {
        spaceDomainService.updateSpace(spaceUpdateRequest);
    }

    @Override
    public void deleteSpace(DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userApplicationService.getLoginUser(request);
        Long spaceId = deleteRequest.getId();
        //判断相册是否存在
        Space oldSpace = this.getById(spaceId);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        //仅本人或管理员可删除
        this.checkSpaceAuth(oldSpace, loginUser);
        //判断用户是否输入了确认删除的信息，管理员不需要输入确认文本
        if (!User.isAdmin(loginUser)){
            ThrowUtils.throwIf(deleteRequest.getDelConfirmInfo() == null, ErrorCode.PARAMS_ERROR, "请输入确认删除文本");
            if (!deleteRequest.getDelConfirmInfo().equals("我确定要删除此相册")){
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "请输入指定的文本");
            }
        }
        //先删除该相册下的所有图片
        List<Long> pictureIdsToDel = pictureApplicationService.getPictureIds(spaceId);
        Boolean isDelPictures = pictureApplicationService.deletePictures(pictureIdsToDel, loginUser);
        ThrowUtils.throwIf(!isDelPictures, ErrorCode.OPERATION_ERROR, "删除相册所有图片失败");
        //操作数据库删除相册
        boolean isDelSpace = this.removeById(spaceId);
        ThrowUtils.throwIf(!isDelSpace, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public Space getSpaceById(long id) {
        return spaceDomainService.getSpaceById(id);
    }

    @Override
    public SpaceVo getSpaceVoById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Space space = this.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        SpaceVo spaceVo = this.getSpaceVo(space, request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        spaceVo.setPermissionList(permissionList);
        return spaceVo;
    }

    @Override
    public Page<Space> listSpaceByPage(SpaceQueryRequest spaceQueryRequest) {
        return spaceDomainService.listSpaceByPage(spaceQueryRequest);
    }

    @Override
    public Page<SpaceVo> listSpaceVoByPage(SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        //查询数据库
        Page<Space> spacePage = this.page(new Page<>(current, size), this.getQueryWrapper(spaceQueryRequest));
        return this.getSpaceVoPage(spacePage, request);
    }

    @Override
    public void editSpace(SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //实体类和DTO进行转换
        Space space = SpaceAssembler.toSpaceEntity(spaceEditRequest);
        //填充数据
        this.fillSpaceBySpaceLevel(space);
        //设置编辑时间
        space.setEditTime(new Date());
        //数据校验
        space.validSpace(false);
        User loginUser = userApplicationService.getLoginUser(request);
        //判断相册是否存在
        Long id = spaceEditRequest.getId();
        Space oldSpace = this.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        //仅本人或管理员可编辑
        this.checkSpaceAuth(oldSpace, loginUser);
        //操作数据库
        boolean result = this.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }
}




