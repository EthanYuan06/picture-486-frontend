package com.yuluo.picture486backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486backend.model.dto.picture.PictureQueryRequest;
import com.yuluo.picture486backend.model.dto.picture.PictureUploadRequest;
import com.yuluo.picture486backend.model.dto.user.UserQueryRequest;
import com.yuluo.picture486backend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.vo.PictureVo;
import jakarta.servlet.http.HttpServletRequest;
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
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser 登录用户（判断权限用）
     * @return 图片信息
     */
    PictureVo uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取单个图片信息
     *
     * @param picture 图片
     * @param request 请求
     * @return 图片信息
     */
    PictureVo getPictureVo(Picture picture, HttpServletRequest request);

    /**
     * 获取图片分页列表
     *
     * @param picturePage 图片分页
     * @param request 请求
     * @return 图片列表
     */
    Page<PictureVo> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 图片数据校验
     * 用于更新和修改图片时判断
     * @param picture 图片信息
     */
    void validPicture(Picture picture);























}
