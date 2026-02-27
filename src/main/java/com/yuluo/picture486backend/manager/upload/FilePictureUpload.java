package com.yuluo.picture486backend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传图片
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate{
    @Override
    protected void validPicture(Object inputSource) {
        //输入源转换为文件类型
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "上传文件为空");
        //校验文件大小
        long fileSize = multipartFile.getSize();
        //定义常量：1MB
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 15 * ONE_MB, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过15MB");
        //获取文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //定义常量：允许的图片格式
        final List<String> ALLOW_FORMAT_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "webp");
        assert fileSuffix != null;
        ThrowUtils.throwIf(!ALLOW_FORMAT_SUFFIX.contains(fileSuffix.toLowerCase()), ErrorCode.PARAMS_ERROR, "不支持的文件类型");
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }
}
