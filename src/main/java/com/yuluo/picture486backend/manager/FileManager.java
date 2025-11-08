package com.yuluo.picture486backend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.yuluo.picture486backend.config.CosClientConfig;
import com.yuluo.picture486backend.exception.BusinessException;
import com.yuluo.picture486backend.exception.ErrorCode;
import com.yuluo.picture486backend.exception.ThrowUtils;
import com.yuluo.picture486backend.model.dto.picture.PictureUploadResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param multipartFile 文件
     * @param uploadPathPrefix 上传路径前缀，例如 user/avatar
     * @return 图片信息
     */
    public PictureUploadResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        //校验图片
        validPicture(multipartFile);
        //构建图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originFilename = multipartFile.getOriginalFilename();//获取文件名
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);
        //上传图片操作
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            //将上传的文件保存到临时文件
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //封装返回结果（设置图片信息）
            PictureUploadResult pictureUploadResult = new PictureUploadResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            //计算图片比例
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            //设置属性
            pictureUploadResult.setPicName(FileUtil.mainName(originFilename));
            pictureUploadResult.setPicSize(FileUtil.size(file));
            pictureUploadResult.setPicWidth(picWidth);
            pictureUploadResult.setPicHeight(picHeight);
            pictureUploadResult.setPicScale(picScale);
            pictureUploadResult.setPicFormat(imageInfo.getFormat());
            pictureUploadResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            // 返回图片信息
            return pictureUploadResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败" , e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTempFile(file, uploadPath);
        }
    }

    /**
     * 删除临时文件
     *
     * @param file 文件
     * @param uploadPath 上传路径
     */
    public void deleteTempFile(File file, String uploadPath) {
        if (file != null) {
            // 删除临时文件
            boolean delete = file.delete();
            if (!delete) {
                log.error("临时文件清理失败，路径：{}", uploadPath);
            }
        }
    }

    /**
     * 校验图片
     *
     * @param multipartFile 文件
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "上传文件为空");
        //校验文件大小
        long fileSize = multipartFile.getSize();
        //定义常量：1MB
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 8 * ONE_MB, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过8MB");
        //获取文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //定义常量：允许的图片格式
        final List<String> ALLOW_FORMAT_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_SUFFIX.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "不支持的文件类型");
    }
}
