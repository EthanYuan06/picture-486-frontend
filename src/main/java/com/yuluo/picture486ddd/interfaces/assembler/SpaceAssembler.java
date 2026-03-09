package com.yuluo.picture486ddd.interfaces.assembler;

import com.yuluo.picture486ddd.domain.space.entity.Space;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceAddRequest;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceEditRequest;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceUpdateRequest;
import org.springframework.beans.BeanUtils;

/**
 * 相册接口转换
 */
public class SpaceAssembler {

    public static Space toSpaceEntity(SpaceAddRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        return space;
    }

    public static Space toSpaceEntity(SpaceUpdateRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        return space;
    }

    public static Space toSpaceEntity(SpaceEditRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        return space;
    }
}
