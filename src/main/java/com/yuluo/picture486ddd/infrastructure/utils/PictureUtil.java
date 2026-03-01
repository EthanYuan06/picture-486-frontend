package com.yuluo.picture486ddd.infrastructure.utils;

import com.qcloud.cos.model.PutObjectResult;
import com.yuluo.picture486ddd.infrastructure.api.CosManager;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.Arrays;
import java.util.Base64;
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
     * 验证文件是否为允许的图片格式（通用Object版本）
     * 支持 MultipartFile、File、String(URL)、byte[] 等多种类型
     * 
     * @param inputSource 输入源对象
     * @return 是否为允许的图片格式
     */
    public static boolean isAllowedImageFormat(Object inputSource) {
        if (inputSource == null) {
            return false;
        }
        
        // 根据不同类型处理
        if (inputSource instanceof MultipartFile) {
            return isAllowedImageFormat((MultipartFile) inputSource);
        } else if (inputSource instanceof File) {
            return isAllowedImageFormat((File) inputSource);
        } else if (inputSource instanceof String) {
            return isAllowedImageFormat((String) inputSource);
        } else if (inputSource instanceof byte[]) {
            return isAllowedImageFormat((byte[]) inputSource);
        } else {
            // 不支持的类型
            return false;
        }
    }
    
    /**
     * 验证MultipartFile是否为允许的图片格式
     * 
     * @param file MultipartFile文件
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
     * 验证File对象是否为允许的图片格式
     * 
     * @param file File对象
     * @return 是否为允许的图片格式
     */
    public static boolean isAllowedImageFormat(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return false;
        }
        
        String fileName = file.getName();
        String fileExtension = getFileExtension(fileName);
        
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");
        return allowedExtensions.contains(fileExtension.toLowerCase());
    }
    
    /**
     * 验证URL字符串是否为允许的图片格式
     * 
     * @param url 图片URL
     * @return 是否为允许的图片格式
     */
    public static boolean isAllowedImageFormat(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        // 提取URL中的文件名
        String fileName = extractFileNameFromUrl(url);
        if (fileName == null) {
            return false;
        }
        
        String fileExtension = getFileExtension(fileName);
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");
        return allowedExtensions.contains(fileExtension.toLowerCase());
    }
    
   

    /**
     * 过滤出符合要求的图片文件
     *
     * @param files 图片文件数组
     * @return 符合要求的图片文件数组
     */
    public static MultipartFile[] filterAllowedImages(MultipartFile[] files) {
        if (files == null) {
            return new MultipartFile[0];
        }
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");
        return Arrays.stream(files)
                .filter(file -> file != null && file.getOriginalFilename() != null)
                .filter(file -> {
                    String fileName = file.getOriginalFilename();
                    String fileExtension = getFileExtension(fileName);
                    return allowedExtensions.contains(fileExtension.toLowerCase());
                })
                .toArray(MultipartFile[]::new);
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
    
    /**
     * 从URL中提取文件名
     * 
     * @param url URL字符串
     * @return 文件名
     */
    private static String extractFileNameFromUrl(String url) {
        if (url == null) {
            return null;
        }
        
        // 处理查询参数
        int queryIndex = url.indexOf('?');
        if (queryIndex != -1) {
            url = url.substring(0, queryIndex);
        }
        
        // 提取路径最后部分
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            return url.substring(lastSlashIndex + 1);
        }
        
        return url;
    }
    
    /**
     * 通过文件头魔数验证图片格式
     * 
     * @param bytes 文件字节数组
     * @return 是否为有效的图片格式
     */
    private static boolean isValidImageMagicNumber(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return false;
        }
        
        // JPEG: FF D8 FF
        if (bytes.length >= 3 && 
            (bytes[0] & 0xFF) == 0xFF && 
            (bytes[1] & 0xFF) == 0xD8 && 
            (bytes[2] & 0xFF) == 0xFF) {
            return true;
        }
        
        // PNG: 89 50 4E 47
        if (bytes.length >= 4 && 
            (bytes[0] & 0xFF) == 0x89 && 
            (bytes[1] & 0xFF) == 0x50 && 
            (bytes[2] & 0xFF) == 0x4E && 
            (bytes[3] & 0xFF) == 0x47) {
            return true;
        }
        
        // WEBP: 52 49 46 46 ... 57 45 42 50
        if (bytes.length >= 12 && 
            (bytes[0] & 0xFF) == 0x52 && 
            (bytes[1] & 0xFF) == 0x49 && 
            (bytes[2] & 0xFF) == 0x46 && 
            (bytes[3] & 0xFF) == 0x46 &&
            (bytes[8] & 0xFF) == 0x57 && 
            (bytes[9] & 0xFF) == 0x45 && 
            (bytes[10] & 0xFF) == 0x42 && 
            (bytes[11] & 0xFF) == 0x50) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 将本地图片文件转换为Base64编码（适用于AI处理等场景）
     * 
     * @param file 本地图片文件
     * @return Base64编码的图片字符串
     */
    public static String convertLocalImageToBase64(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }
        
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            
            byte[] imageBytes = bos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("图片转换Base64失败", e);
        }
    }
    
    /**
     * 将MultipartFile转换为Base64编码
     * 
     * @param multipartFile 上传的文件
     * @return Base64编码的图片字符串
     */
    public static String convertMultipartFileToBase64(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        try (InputStream inputStream = multipartFile.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("图片转换Base64失败", e);
        }
    }


}