package com.yuluo.picture486backend.service;

import com.yuluo.picture486backend.model.dto.picture.UploadPictureRequest;
import com.yuluo.picture486backend.model.dto.picture.UploadPictureResult;
import com.yuluo.picture486backend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.vo.PictureVo;
import org.springframework.web.multipart.MultipartFile;

/**
* @author 东山千夏
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-11-07 22:39:28
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param multipartFile 文件
     * @param uploadPictureRequest 图片上传请求
     * @param loginUser 登录用户（判断权限用）
     * @return 图片信息
     */
    PictureVo uploadPicture(MultipartFile multipartFile, UploadPictureRequest uploadPictureRequest, User loginUser);
}
