package com.yuluo.picture486backend.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.yuluo.picture486ddd.infrastructure.annotation.AuthCheck;
import com.yuluo.picture486ddd.infrastructure.common.BaseResponse;
import com.yuluo.picture486ddd.infrastructure.common.ResultUtils;
import com.yuluo.picture486ddd.domain.user.constant.UserConstant;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.infrastructure.api.CosManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/file")
@Tag(name = "文件处理模块")
public class FileController {
    @Resource
    private CosManager cosManager;

    /**
     * 测试文件上传
     *
     * @param multipartFile  文件
     * @return 文件路径
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    @Operation(summary = "测试文件上传")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 文件目录
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putPictureObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }
    
    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    @Operation(summary = "测试文件下载")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getPictureObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }

//    /**
//     * 触发WebP图片转换任务
//     *
//     * @return 操作结果
//     */
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    @PostMapping("/webp/process")
//    @Operation(summary = "处理WebP图片转换", description = "扫描public目录下的所有图片并生成对应的WebP版本")
//    public BaseResponse<Boolean> processWebpImages() {
//        try {
//            WebpUtils.processPublicImagesToWebp();
//            return ResultUtils.success(true);
//        } catch (Exception e) {
//            log.error("WebP转换任务失败", e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "WebP转换失败");
//        }
//    }
//
//    /**
//     * 触发缩略图生成任务
//     *
//     * @return 操作结果
//     */
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    @PostMapping("/thumbnail/process")
//    @Operation(summary = "处理缩略图生成", description = "扫描存储桶中的所有图片并生成对应的缩略图")
//    public BaseResponse<Boolean> processThumbnailImages() {
//        try {
//            ThumbnailUtils.processAllImagesToThumbnail();
//            return ResultUtils.success(true);
//        } catch (Exception e) {
//            log.error("缩略图转换任务失败", e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "缩略图转换失败");
//        }
//    }
//
//    /**
//     * 扫描并存储缩略图URL
//     *
//     * @return 操作结果
//     */
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    @PostMapping("/thumbnail/scan")
//    @Operation(summary = "扫描并存储缩略图URL", description = "扫描存储桶中已存在的缩略图并将URL存入数据库")
//    public BaseResponse<Boolean> scanAndStoreThumbnails() {
//        try {
//            ThumbnailUtils.scanAndStoreAllThumbnails();
//            return ResultUtils.success(true);
//        } catch (Exception e) {
//            log.error("扫描并存储缩略图任务失败", e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "扫描并存储缩略图失败");
//        }
//    }
}