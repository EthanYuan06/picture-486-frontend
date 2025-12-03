package com.yuluo.picture486backend.utils;

import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;

/**
 * 图片工具类
 */
public class PictureUtil {
    
    /**
     * 计算批量上传图片的总大小
     * 
     * @param files 图片文件数组
     * @return 总大小（字节）
     */
    public static long calculateTotalSize(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return 0L;
        }
        
        long totalSize = 0L;
        for (MultipartFile file : files) {
            if (file != null) {
                totalSize += file.getSize();
            }
        }
        return totalSize;
    }
    
    /**
     * 验证文件是否为允许的图片格式
     * 
     * @param file 文件
     * @return 是否为允许的图片格式
     */
    public static boolean isAllowedImageFormat(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return false;
        }
        
        String fileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(fileName);
        
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");
        return allowedExtensions.contains(fileExtension.toLowerCase());
    }
    
    /**
     * 从文件名中提取文件扩展名
     * 
     * @param fileName 文件名
     * @return 文件扩展名（小写）
     */
    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}