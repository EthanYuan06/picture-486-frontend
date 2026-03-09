package com.yuluo.picture486ddd.domain.space.service.impl;


import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486ddd.domain.space.repository.SpaceRepository;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import com.yuluo.picture486ddd.interfaces.assembler.SpaceAssembler;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceQueryRequest;
import com.yuluo.picture486ddd.domain.space.entity.Space;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.domain.space.valueobject.SpaceLevelEnum;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceUpdateRequest;
import com.yuluo.picture486ddd.domain.space.service.SpaceDomainService;
import com.yuluo.picture486ddd.infrastructure.mapper.SpaceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
* @author 东山羽洛
*/
@Service
public class SpaceDomainServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceDomainService {
    @Resource
    private SpaceRepository spaceRepository;
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
        if (!space.getUserId().equals(loginUser.getId()) && !User.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

    @Override
    public void updateSpace(SpaceUpdateRequest spaceUpdateRequest) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //将实体类和DTO进行转换
        Space space = SpaceAssembler.toSpaceEntity(spaceUpdateRequest);
        //填充数据
        this.fillSpaceBySpaceLevel(space);
        //数据校验
        space.validSpace(false);
        //判断相册是否存在
        Long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceRepository.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        //操作数据库
        boolean result = spaceRepository.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public Space getSpaceById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Space space = spaceRepository.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return space;
    }

    @Override
    public Page<Space> listSpaceByPage(SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        //查询数据库
        return this.page(
                new Page<>(current, size),
                this.getQueryWrapper(spaceQueryRequest));
    }

}




