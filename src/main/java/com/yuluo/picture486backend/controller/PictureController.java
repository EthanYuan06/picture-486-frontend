package com.yuluo.picture486backend.controller;

import com.yuluo.picture486backend.annotation.AuthCheck;
import com.yuluo.picture486backend.common.BaseResponse;
import com.yuluo.picture486backend.common.ResultUtils;
import com.yuluo.picture486backend.constant.UserConstant;
import com.yuluo.picture486backend.exception.ErrorCode;
import com.yuluo.picture486backend.exception.ThrowUtils;
import com.yuluo.picture486backend.model.dto.picture.UploadPictureRequest;
import com.yuluo.picture486backend.model.dto.user.UserRegisterRequest;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.vo.PictureVo;
import com.yuluo.picture486backend.service.PictureService;
import com.yuluo.picture486backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/picture")
@Tag(name = "图片模块")
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @PostMapping("/upload")
    @Operation(summary = "上传图片")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVo> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            UploadPictureRequest uploadPictureRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVo pictureVo = pictureService.uploadPicture(multipartFile, uploadPictureRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }
}
