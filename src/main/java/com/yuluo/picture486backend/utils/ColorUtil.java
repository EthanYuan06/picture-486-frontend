package com.yuluo.picture486backend.utils;

/**
 * 颜色工具类
 */
public class ColorUtil {

    /**
     * 将十六进制颜色代码转换为RGBA文本格式
     * @param hex 十六进制颜色代码，例如 #FF0000
     * @return RGBA文本格式，例如 rgba(255, 0, 0, 1)
     */
    public static String hexToRgba(String hex) {
        if (hex == null || hex.isEmpty()) {
            throw new IllegalArgumentException("十六进制颜色代码不能为空");
        }
        
        // 移除可能存在的 # 符号
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        
        // 验证长度
        if (hex.length() != 6 && hex.length() != 8) {
            throw new IllegalArgumentException("无效的十六进制颜色代码长度，应为6位或8位");
        }
        
        try {
            // 解析RGB值
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            
            // 如果有alpha通道
            if (hex.length() == 8) {
                double a = Integer.parseInt(hex.substring(6, 8), 16) / 255.0;
                return String.format("rgba(%d, %d, %d, %.2f)", r, g, b, a);
            } else {
                return String.format("rgba(%d, %d, %d, 1)", r, g, b);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的十六进制颜色代码: " + hex, e);
        }
    }
}
