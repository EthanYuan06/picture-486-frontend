package com.yuluo.picture486backend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
  
    /**  
     * 图片 id
     */  
    private Long id;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 相册 id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;  
}
