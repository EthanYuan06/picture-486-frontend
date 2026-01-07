package com.yuluo.picture486backend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yuluo.picture486backend.annotation.AuthCheck;
import com.yuluo.picture486backend.common.BaseResponse;
import com.yuluo.picture486backend.common.DeleteRequest;
import com.yuluo.picture486backend.common.ResultUtils;
import com.yuluo.picture486backend.constant.UserConstant;
import com.yuluo.picture486backend.exception.BusinessException;
import com.yuluo.picture486backend.exception.ErrorCode;
import com.yuluo.picture486backend.exception.ThrowUtils;
import com.yuluo.picture486backend.model.dto.picture.*;
import com.yuluo.picture486backend.model.entity.Picture;
import com.yuluo.picture486backend.model.entity.Space;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.enums.PictureReviewStatusEnum;
import com.yuluo.picture486backend.model.vo.PictureTagCategory;
import com.yuluo.picture486backend.model.vo.PictureVo;
import com.yuluo.picture486backend.service.PictureService;
import com.yuluo.picture486backend.manager.CacheManager;
import com.yuluo.picture486backend.service.SpaceService;
import com.yuluo.picture486backend.service.UserService;
import com.yuluo.picture486backend.utils.PictureUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/picture")
@Tag(name = "图片模块")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private SpaceService spaceService;

    @PostMapping("/upload")
    @Operation(summary = "上传图片")
    public BaseResponse<PictureVo> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        //用户校验
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //校验是否是支持的图片格式
        ThrowUtils.throwIf(!PictureUtil.isAllowedImageFormat(multipartFile), ErrorCode.PARAMS_ERROR, "图片格式不支持");
        PictureVo pictureVo = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }


    @Deprecated
    @PostMapping("/upload/url")
    @Operation(summary = "url上传图片")
    public BaseResponse<PictureVo> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVo pictureVo = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除图片")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long pictureId = deleteRequest.getId();
        pictureService.deletePicture(pictureId, loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @Operation(summary = "【管理员】更新图片")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //将实体类和DTO进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        //list转换为string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        //数据校验
        pictureService.validPicture(picture);
        //判断图片是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewPictureParams(picture, loginUser);
        //操作数据库更新图片信息
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "【管理员】根据id获取图片")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }

    @GetMapping("/get/vo")
    @Operation(summary = "根据id获取图片（脱敏）")
    public BaseResponse<PictureVo> getPictureVoById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        //相册权限校验
        Long spaceId = picture.getSpaceId();
        if (spaceId != null){
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }
        return ResultUtils.success(pictureService.getPictureVo(picture, request));
    }


    @PostMapping("/list/page")
    @Operation(summary = "【管理员】分页获取图片列表")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
//        return ResultUtils.success(cacheManager.getCachedResult(
//                "listPictureByPage",
//                pictureQueryRequest,
//                () -> {
//                    long current = pictureQueryRequest.getCurrent();
//                    long size = pictureQueryRequest.getPageSize();
//                    return pictureService.page(new Page<>(current, size),
//                            pictureService.getQueryWrapper(pictureQueryRequest));
//                }
//        ));
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        //公开图库
        Long spaceId = pictureQueryRequest.getSpaceId();
        if(spaceId == null){
            pictureQueryRequest.setNullSpaceId(true);
        }else {
            //私有相册
            //校验相册是否存在
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "相册不存在");
        }
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    @PostMapping("/list/page/vo")
    @Operation(summary = "分页获取图片列表")
    public BaseResponse<Page<PictureVo>> listPictureVoByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        //限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //相册权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        //公开图库
        if(spaceId == null){
            //普通用户只能查看已过审的数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        }else {
            //私有相册
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "相册不存在");
            if (!space.getUserId().equals(loginUser.getId()) && !loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该相册");
            }
        }
        
//        return ResultUtils.success(cacheManager.getCachedResult(
//                "listPictureVoByPage",
//                pictureQueryRequest,
//                () -> {
//                    Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
//                            pictureService.getQueryWrapper(pictureQueryRequest));
//                    return pictureService.getPictureVoPage(picturePage, request);
//                }
//        ));
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPictureVoPage(picturePage, request));
    }


    @PostMapping("/edit")
    @Operation(summary = "编辑图片")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/review")
    @Operation(summary = "【管理员】图片审核")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/review/batch")
    @Operation(summary = "【管理员】图片批量审核")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReviewByBatch(@RequestBody PictureReviewByBatchRequest pictureReviewByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReviewByBatch(pictureReviewByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/edit/batch")
    @Operation(summary = "批量编辑图片")
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPictures(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传图片")
    public BaseResponse<List<PictureVo>> uploadPictureByBatch(
            @RequestPart("file") MultipartFile[] multipartFiles,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        //用户校验
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 参数校验
        ThrowUtils.throwIf(multipartFiles == null || multipartFiles.length == 0, ErrorCode.PARAMS_ERROR, "未选择任何文件");
        ThrowUtils.throwIf(multipartFiles.length > 10, ErrorCode.PARAMS_ERROR, "单次上传文件数量不能超过10个");
        //格式校验，只上传符合后缀要求的图片
        MultipartFile[] AllowedMultipartFiles = PictureUtil.filterAllowedImages(multipartFiles);
        log.info("实际上传图片数量：{}", AllowedMultipartFiles.length);
        // 批量上传处理
        List<PictureVo> pictureVos = pictureService.uploadPictures(AllowedMultipartFiles, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVos);
    }

    @PostMapping("/delete/batch")
    @Operation(summary = "批量删除图片")
    public BaseResponse<Boolean> deletePictureByBatch(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<Long> PictureIds = deleteRequest.getIds();
        //参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 双重判断ids是否传递，以及传递了是否为空列表
        if (PictureIds == null || PictureIds.isEmpty()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未选择图片");
        }
        //批量删除图片
        Boolean result = pictureService.deletePictures(PictureIds, loginUser);
        return ResultUtils.success(result);
    }
    @GetMapping("/tag_category")
    @Operation(summary = "标签和分类")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("动漫", "日本旅行", "风景", "表情", "壁纸");
        List<String> categoryList = Arrays.asList("模板", "二次元", "回忆", "素材", "旅游");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();
















}
