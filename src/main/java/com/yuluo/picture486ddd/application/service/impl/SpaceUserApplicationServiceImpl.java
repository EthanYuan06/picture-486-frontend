package com.yuluo.picture486ddd.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486backend.model.dto.space_user.SpaceUserAddRequest;
import com.yuluo.picture486backend.model.dto.space_user.SpaceUserQueryRequest;
import com.yuluo.picture486ddd.application.service.SpaceUserApplicationService;
import com.yuluo.picture486ddd.application.service.UserApplicationService;
import com.yuluo.picture486ddd.domain.space.entity.Space;
import com.yuluo.picture486ddd.domain.space.entity.SpaceUser;
import com.yuluo.picture486ddd.domain.space.service.SpaceDomainService;
import com.yuluo.picture486ddd.domain.space.service.SpaceUserDomainService;
import com.yuluo.picture486ddd.domain.space.valueobject.SpaceRoleEnum;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486ddd.infrastructure.mapper.SpaceUserMapper;
import com.yuluo.picture486ddd.interfaces.vo.space.SpaceUserVo;
import com.yuluo.picture486ddd.interfaces.vo.space.SpaceVo;
import com.yuluo.picture486ddd.interfaces.vo.user.UserVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 东山羽洛
*/
@Service
public class SpaceUserApplicationServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserApplicationService {

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    @Lazy
    private SpaceDomainService spaceDomainService;

    @Resource
    @Lazy
    private SpaceUserDomainService spaceUserDomainService;

    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        validSpaceUser(spaceUser, true);
        // 插入数据
        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return spaceUser.getId();
    }

    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        //获取相册id与用户id
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        //仅在添加时，根据相册id与用户id查询记录是否存在，并校验成员是否已经添加过
        if (add) {
            ThrowUtils.throwIf(spaceId == null || userId == null, ErrorCode.PARAMS_ERROR);
            User user = userApplicationService.getUser(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在");
            //判断用户是否已经在相册中，如果spaceId与userId同时匹配的记录存在，则用户已加入该相册
            SpaceUser oldSpaceUser = this.getOne(new QueryWrapper<SpaceUser>().eq("spaceId", spaceId).eq("userId", userId));
            ThrowUtils.throwIf(oldSpaceUser != null, ErrorCode.OPERATION_ERROR, "用户已加入该相册");
            Space space = spaceDomainService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR, "相册不存在");
        }
        //校验相册角色
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        if (spaceRole != null && spaceRoleEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "相册角色不存在");
        }
    }

    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        return spaceUserDomainService.getQueryWrapper(spaceUserQueryRequest);
    }

    @Override
    public SpaceUserVo getSpaceUserVo(SpaceUser spaceUser, HttpServletRequest request) {
        //对象转封装类
        SpaceUserVo spaceUserVo = SpaceUserVo.objToVo(spaceUser);
        //关联查询用户信息
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0){
            User user = userApplicationService.getUser(userId);
            UserVo userVo = userApplicationService.getUserVo(user);
            spaceUserVo.setUser(userVo);
        }
        //关联查询相册信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0){
            Space space = spaceDomainService.getById(spaceId);
            SpaceVo spaceVo = spaceDomainService.getSpaceVo(space, request);
            spaceUserVo.setSpace(spaceVo);
        }
        return spaceUserVo;
    }

    @Override
    public List<SpaceUserVo> getSpaceUserVoList(List<SpaceUser> spaceUserList) {
        //判断输入列表是否为空
        if (CollUtil.isEmpty(spaceUserList)) {
            return new ArrayList<>();
        }
        //对象列表转换为封装对象列表
        List<SpaceUserVo> spaceUserVoList = spaceUserList.stream().map(SpaceUserVo::objToVo).toList();
        //收集需要关联查询的用户id与相册id
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        //批量查询用户和空间
        Map<Long, List<User>> userIdUserListMap = userApplicationService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceDomainService.listByIds(spaceIdSet).stream().collect(Collectors.groupingBy(Space::getId));
        //填充SpaceUserVo的用户和空间信息
        spaceUserVoList.forEach(spaceUserVo -> {
            Long userId = spaceUserVo.getUserId();
            Long spaceId = spaceUserVo.getSpaceId();
            //填充用户信息
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceUserVo.setUser(userApplicationService.getUserVo(user));
            //填充空间信息
            Space space = null;
            if (spaceIdSpaceListMap.containsKey(spaceId)) {
                space = spaceIdSpaceListMap.get(spaceId).get(0);
            }
            spaceUserVo.setSpace(SpaceVo.objToVo(space));
        });
        return spaceUserVoList;
    }
}




