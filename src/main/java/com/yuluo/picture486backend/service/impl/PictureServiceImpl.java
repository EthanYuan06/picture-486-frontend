package com.yuluo.picture486backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486backend.exception.BusinessException;
import com.yuluo.picture486backend.exception.ErrorCode;
import com.yuluo.picture486backend.exception.ThrowUtils;
import com.yuluo.picture486backend.manager.CosManager;
import com.yuluo.picture486backend.manager.upload.FilePictureUpload;
import com.yuluo.picture486backend.manager.upload.PictureUploadTemplate;
import com.yuluo.picture486backend.manager.upload.UrlPictureUpload;
import com.yuluo.picture486backend.model.dto.picture.*;
import com.yuluo.picture486backend.model.entity.Picture;
import com.yuluo.picture486backend.model.entity.Space;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.enums.PictureReviewStatusEnum;
import com.yuluo.picture486backend.model.vo.PictureVo;
import com.yuluo.picture486backend.model.vo.UserVo;
import com.yuluo.picture486backend.service.PictureService;
import com.yuluo.picture486backend.mapper.PictureMapper;
import com.yuluo.picture486backend.service.SpaceService;
import com.yuluo.picture486backend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 东山羽洛
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{
    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private SpaceService spaceService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public PictureVo uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        if (inputSource == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片为空");
        }
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            //必须是空间创建人才能上传
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该空间");
            }
            //校验额度
            if (space.getTotalCount() >= space.getMaxCount()){
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "超出最大图片存储数量");
            }
            //校验容量
            if (space.getTotalSize() >= space.getMaxSize()){
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间剩余容量不足");
            }
        }
        //判断是新增还是删除
        Long pictureId;
        pictureId = pictureUploadRequest.getId();
        //若更新，则判断图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //仅本人或管理员可编辑
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该图片");
            }
            //校验空间是否一致
            //未传递spaceId，则复用原有图片的spaceId
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                //若传递spaceId，则校验spaceId是否与图片的spaceId一致
                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间id不一致");
                }
            }
        }
            //上传图片
            //按照用户id划分目录（公共空间）；按照空间id划分目录（私有空间）
            String uploadPathPrefix;
            if (spaceId != null){
                uploadPathPrefix = String.format("space/%s", spaceId);
            }else{
                uploadPathPrefix = String.format("public/%s", loginUser.getId());
            }
            //根据inputSource区分上传方式
            PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
            if (inputSource instanceof String) {
                pictureUploadTemplate = urlPictureUpload;
            }
            PictureUploadResult pictureUploadResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
            //构造要入库的图片信息
            Picture picture = new Picture();
            picture.setUrl(pictureUploadResult.getUrl());
            picture.setThumbnailUrl(pictureUploadResult.getThumbnailUrl());
            picture.setName(pictureUploadResult.getPicName());
            picture.setPicSize(pictureUploadResult.getPicSize());
            picture.setPicWidth(pictureUploadResult.getPicWidth());
            picture.setPicHeight(pictureUploadResult.getPicHeight());
            picture.setPicScale(pictureUploadResult.getPicScale());
            picture.setPicFormat(pictureUploadResult.getPicFormat());
            picture.setUserId(loginUser.getId());
            picture.setSpaceId(spaceId);
            //填充审核参数
            this.fillReviewPictureParams(picture, loginUser);
            //如果pictureId不为空，表示更新，否则是新增
            //若更新，则补充id和编辑时间
            if (pictureId != null) {
                picture.setId(pictureId);
                picture.setEditTime(new Date());
            }
            //开启事务
            Long finalSpaceId = spaceId;
            transactionTemplate.execute(status -> {
                //插入数据
                boolean result = this.saveOrUpdate(picture);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
                if (finalSpaceId != null) {
                    //更新空间剩余额度
                    boolean update = spaceService.lambdaUpdate()
                            .eq(Space::getId, finalSpaceId)
                            .setSql("totalSize = totalSize + " + picture.getPicSize())
                            .setSql("totalCount = totalCount + 1")
                            .update();
                    ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "图片额度更新失败");
                }
                return picture;
            });
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
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        String createTimeStart = pictureQueryRequest.getCreateTimeStart();
        String createTimeEnd = pictureQueryRequest.getCreateTimeEnd();
        String editTimeStart = pictureQueryRequest.getEditTimeStart();
        String editTimeEnd = pictureQueryRequest.getEditTimeEnd();
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
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        // 只有当审核状态不为空且不是"全部"状态时，才添加审核状态查询条件
        if (reviewStatus != null) {
            queryWrapper.eq("reviewStatus", reviewStatus);
        }
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
        // 创建时间范围查询
        if (createTimeStart != null || createTimeEnd != null) {
            DateTime startTime = DateUtil.parse(createTimeStart, "yyyy-MM-dd");
            DateTime endTime = DateUtil.parse(createTimeEnd, "yyyy-MM-dd");
            DateTime startOfDay = DateUtil.beginOfDay(startTime);
            DateTime endOfDay = DateUtil.endOfDay(endTime);
            queryWrapper.between("createTime", startOfDay, endOfDay);
        }
        // 编辑时间范围查询
        if (editTimeStart != null || editTimeEnd != null) {
            DateTime startTime = DateUtil.parse(editTimeStart, "yyyy-MM-dd");
            DateTime endTime = DateUtil.parse(editTimeEnd, "yyyy-MM-dd");
            DateTime startOfDay = DateUtil.beginOfDay(startTime);
            DateTime endOfDay = DateUtil.endOfDay(endTime);
            queryWrapper.between("editTime", startOfDay, endOfDay);
        }
        //排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("asc"), sortField);
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
     * 删除图片
     * @param oldPicture 旧图片信息
     */
    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        //判断图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        Long count = this.lambdaQuery()
                .eq(Picture::getUrl, pictureUrl)
                .count();
        //若被多条记录使用，不清理
        if(count > 1){
            log.info("图片被多条记录使用，不进行清理");
            return;
        }
        try {
            cosManager.deleteObject(oldPicture.getUrl());
            // 删除webp格式图片
            String webpUrl = oldPicture.getUrl();
            if (!webpUrl.endsWith(".webp")) {
                // 获取文件名（不含扩展名）
                int lastDotIndex = webpUrl.lastIndexOf(".");
                if (lastDotIndex != -1) {
                    webpUrl = webpUrl.substring(0, lastDotIndex) + ".webp";
                    try {
                        cosManager.deleteObject(webpUrl);
                    } catch (Exception e) {
                        log.error("删除webp图片文件失败: url={}, 错误信息={}", webpUrl, e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("删除图片文件失败: url={}, 错误信息={}", pictureUrl, e.getMessage(), e);
        }
        //清理缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if(StrUtil.isNotBlank(thumbnailUrl)){
            try {
                cosManager.deleteObject(thumbnailUrl);
            } catch (Exception e) {
                log.error("删除缩略图文件失败: url={}, 错误信息={}", thumbnailUrl, e.getMessage(), e);
            }
        }
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null){
            //公共图库，仅本人和管理员可删除
            if (!userService.isAdmin(loginUser) && !loginUser.getId().equals(picture.getUserId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }else {
            //私有图库，仅空间管理员可删除
            if (!picture.getUserId().equals(loginUser.getId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断图片是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //校验权限
        checkPictureAuth(loginUser, oldPicture);
        //开启事务
        Long spaceId = oldPicture.getSpaceId();
        transactionTemplate.execute(status -> {
            //操作数据库删除图片
            boolean result = this.removeById(oldPicture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片删除失败");
            if (spaceId != null) {
                //更新空间剩余额度
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, oldPicture.getSpaceId())
                        .setSql("totalSize = totalSize - " + oldPicture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "图片额度更新失败");
            }
            return true;
        });
        //删除存储桶图片
        this.clearPictureFile(oldPicture);
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser){
        //实体类和DTO进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        //list转换为string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        //设置编辑时间
        picture.setEditTime(new Date());
        //设置空间id
        picture.setSpaceId(pictureEditRequest.getSpaceId());
        //数据校验
        this.validPicture(picture);
        //判断图片是否存在
        Long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //校验权限
        checkPictureAuth(loginUser, oldPicture);
        //补充审核参数
        this.fillReviewPictureParams(oldPicture, loginUser);
        //操作数据库
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }
}




