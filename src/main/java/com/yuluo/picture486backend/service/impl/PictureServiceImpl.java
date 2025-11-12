package com.yuluo.picture486backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486backend.exception.BusinessException;
import com.yuluo.picture486backend.exception.ErrorCode;
import com.yuluo.picture486backend.exception.ThrowUtils;
import com.yuluo.picture486backend.manager.FileManager;
import com.yuluo.picture486backend.model.dto.picture.PictureQueryRequest;
import com.yuluo.picture486backend.model.dto.picture.PictureReviewRequest;
import com.yuluo.picture486backend.model.dto.picture.PictureUploadRequest;
import com.yuluo.picture486backend.model.dto.picture.PictureUploadResult;
import com.yuluo.picture486backend.model.entity.Picture;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.enums.PictureReviewStatusEnum;
import com.yuluo.picture486backend.model.vo.PictureVo;
import com.yuluo.picture486backend.model.vo.UserVo;
import com.yuluo.picture486backend.service.PictureService;
import com.yuluo.picture486backend.mapper.PictureMapper;
import com.yuluo.picture486backend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Resource
    private UserService userService;
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
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //仅管理员和本人可以编辑
            if (!loginUser.getId().equals(oldPicture.getUserId()) && !userService.isAdmin(loginUser)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        //上传图片
        //按照用户id划分目录（构建上传路径前缀，指定存储桶中的存储路径）
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        PictureUploadResult pictureUploadResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        //构造要入库的图片信息
        Picture picture = getPictureInfo(loginUser, pictureUploadResult, pictureId);
        //若pictureId不为空，表示更新，否则是新增
        if (pictureId != null){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        //填充审核参数
        this.fillReviewPictureParams(picture, loginUser);
        //保存图片信息到数据库
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVo.objToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        //创建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        //从对象中获取参数
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        //从多字段中搜索，支持同时从name和introduction中检索
        if(StrUtil.isNotBlank(searchText)){
            //拼接查询条件
            queryWrapper.and(qw ->
                    qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText));
        }
        //定义查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        //JSON数组查询
        /*
        and (tag like "%\"Java\"%" and like "%\"Python\"%")
         */
        if (CollUtil.isNotEmpty(tags)){
            for (String tag : tags){
                queryWrapper.like("tags", "\"" + tag + "\"");//转义格式
            }
        }
        //排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVo getPictureVo(Picture picture, HttpServletRequest request) {
        //picture对象转换为Vo
        PictureVo pictureVo = PictureVo.objToVo(picture);
        //关联用户查询信息
        Long userId = picture.getUserId();
        if(userId != null && userId > 0){
            User user = userService.getById(userId);
            UserVo userVo = userService.getUserVo(user);
            pictureVo.setUser(userVo);
        }
        return pictureVo;
    }

    @Override
    public Page<PictureVo> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request) {
        //获取图片列表
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVo> pictureVoPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)){
            return pictureVoPage;
        }
        //从图片列表中获取每个图片对象，转换为Vo脱敏，再统一封装到新的列表
        List<PictureVo> pictureVoList = pictureList.stream().map(PictureVo::objToVo).toList();
        //关联查询用户信息（去重，使用Set集合）
        //从图片列表中提取所有不重复的用户ID
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        //批量获取用户信息，并按用户ID进行分组
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        //填充信息
        pictureVoList.forEach(pictureVo -> {
            Long userId = pictureVo.getUserId();
            User user = null;
            if(userIdUserListMap.containsKey(userId)){
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVo.setUser(userService.getUserVo(user));
        });
        pictureVoPage.setRecords(pictureVoList);
        return pictureVoPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        //从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        //修改数据时，id不能为空，有参数则校验
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        if(StrUtil.isNotBlank(url)){
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url过长");
        }
        if(StrUtil.isNotBlank(introduction)){
            ThrowUtils.throwIf(introduction.length() > 1024, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //1.校验参数
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || reviewStatus == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //3.判断图片是否重复审核
        if (oldPicture.getReviewStatus().equals(reviewStatus)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请勿重复审核");
        }
        //4.操作数据库更新审核状态
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewStatus(reviewStatus);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "操作失败");
    }

    @Override
    public void fillReviewPictureParams(Picture picture, User loginUser){
        if (userService.isAdmin(loginUser)){
            //管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewTime(new Date());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
        }else {
            //普通用户创建或编辑图片，只更改字段为“待审核”
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
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




