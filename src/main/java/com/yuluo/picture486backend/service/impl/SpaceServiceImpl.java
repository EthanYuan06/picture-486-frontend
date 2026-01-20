package com.yuluo.picture486backend.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486backend.constant.SpaceConstant;
import com.yuluo.picture486backend.exception.BusinessException;
import com.yuluo.picture486backend.exception.ErrorCode;
import com.yuluo.picture486backend.exception.ThrowUtils;
import com.yuluo.picture486backend.model.dto.space.SpaceAddRequest;
import com.yuluo.picture486backend.model.dto.space.SpaceQueryRequest;
import com.yuluo.picture486backend.model.entity.Picture;
import com.yuluo.picture486backend.model.entity.Space;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.enums.SpaceLevelEnum;
import com.yuluo.picture486backend.model.enums.SpaceTypeEnum;
import com.yuluo.picture486backend.model.vo.PictureVo;
import com.yuluo.picture486backend.model.vo.SpaceVo;
import com.yuluo.picture486backend.model.vo.UserVo;
import com.yuluo.picture486backend.service.SpaceService;
import com.yuluo.picture486backend.mapper.SpaceMapper;
import com.yuluo.picture486backend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
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
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        //dto转实体类
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        //设置默认相册名称、等级以及相册类型
        if (StrUtil.isBlank(spaceAddRequest.getSpaceName())){
            space.setSpaceName(SpaceConstant.DEFAULT_SPACE_NAME);
        }
        if (spaceAddRequest.getSpaceLevel() == null){
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (spaceAddRequest.getSpaceType() == null){
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        //设置默认限额与封面
        this.fillSpaceBySpaceLevel(space);
        space.setSpaceCover(SpaceConstant.DEFAULT_SPACE_COVER);
        //数据校验
        this.validSpace(space, true);
        Long userId = loginUser.getId();
        //校验权限，不允许非管理员创建默认级别以上的相册
        if (!userService.isAdmin(loginUser) && space.getSpaceLevel() != SpaceLevelEnum.COMMON.getValue()){
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
            if (!userService.isAdmin(loginUser) && space.getSpaceType().equals(SpaceTypeEnum.PRIVATE.getValue())) {
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
    public void validSpace(Space space, boolean isAdd) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        //从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        //判断是否是创建相册
        if (isAdd) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册名不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册等级不能为空");
            }
            if(spaceType == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册类型不能为空");
            }
        }
        //修改数据时，更改相册级别时的判定
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册等级不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 24) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册名称过长");
        }
        if(spaceType != null && spaceTypeEnum == null){
            //随意输入一个数字绕过spaceType != null也不能绕过spaceTypeEnum == null的结果
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册类型不存在");
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        //创建查询条件
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        //从对象中获取参数
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        //定义查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        
        //排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public SpaceVo getSpaceVo(Space space, HttpServletRequest request) {
        //space对象转换为Vo
        SpaceVo spaceVo = SpaceVo.objToVo(space);
        //关联用户查询信息
        Long userId = space.getUserId();
        if(userId != null && userId > 0){
            User user = userService.getById(userId);
            UserVo userVo = userService.getUserVo(user);
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
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        //3.填充信息
        spaceVoList.forEach(spaceVo -> {
            Long userId = spaceVo.getUserId();
            User user = null;
            if(userIdUserListMap.containsKey(userId)){
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVo.setUser(userService.getUserVo(user));
        });
        spaceVoPage.setRecords(spaceVoList);
        return spaceVoPage;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        //获取相册级别
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        //根据相册级别填充限额
        if (spaceLevelEnum != null){
            //获取容量限额
            long maxSize = spaceLevelEnum.getMaxSize();
            //管理员未指定时，使用默认值
            if (space.getMaxSize() == null){
                space.setMaxSize(maxSize);
            }
            //获取数量限额
            long maxCount = spaceLevelEnum.getMaxCount();
            //管理员未指定时，使用默认值
            if (space.getMaxCount() == null){
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public void checkSpaceAuth(Space space, User loginUser) {
        //仅相册创建人或管理员可操作
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}




