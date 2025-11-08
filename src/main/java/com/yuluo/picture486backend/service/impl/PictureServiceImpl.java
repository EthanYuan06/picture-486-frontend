package com.yuluo.picture486backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486backend.exception.ErrorCode;
import com.yuluo.picture486backend.exception.ThrowUtils;
import com.yuluo.picture486backend.manager.FileManager;
import com.yuluo.picture486backend.model.dto.picture.PictureUploadRequest;
import com.yuluo.picture486backend.model.dto.picture.PictureUploadResult;
import com.yuluo.picture486backend.model.entity.Picture;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.vo.PictureVo;
import com.yuluo.picture486backend.service.PictureService;
import com.yuluo.picture486backend.mapper.PictureMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
* @author 东山千夏
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-11-07 22:39:28
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{
    @Resource
    private FileManager fileManager;
    @Override
    public PictureVo uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断新增还是删除
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        //若更新，则判断图片是否存在
        if (pictureId != null){
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        //上传图片
        //按照用户id划分目录（构建上传路径前缀，指定存储桶中的存储路径）
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        PictureUploadResult pictureUploadResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        //构造要入库的图片信息
        Picture picture = getPictureInfo(loginUser, pictureUploadResult, pictureId);
        //保存图片信息到数据库
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVo.objToVo(picture);
    }

    /**
     * 获取图片信息
     * @param loginUser 登录用户
     * @param pictureUploadResult 上传图片结果
     * @param pictureId 图片id
     * @return 图片信息
     */

    private static Picture getPictureInfo(User loginUser, PictureUploadResult pictureUploadResult, Long pictureId) {
        Picture picture = new Picture();
        picture.setUrl(pictureUploadResult.getUrl());
        picture.setName(pictureUploadResult.getPicName());
        picture.setPicSize(pictureUploadResult.getPicSize());
        picture.setPicWidth(pictureUploadResult.getPicWidth());
        picture.setPicHeight(pictureUploadResult.getPicHeight());
        picture.setPicScale(pictureUploadResult.getPicScale());
        picture.setPicFormat(pictureUploadResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        //如果pictureId不为空，表示更新，否则是新增
        if (pictureId != null) {
            //若更新，则补充id和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        return picture;
    }
}




