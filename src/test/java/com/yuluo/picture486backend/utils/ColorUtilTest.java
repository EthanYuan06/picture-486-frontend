package com.yuluo.picture486backend.utils;

import com.yuluo.picture486ddd.infrastructure.utils.ColorUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ColorUtil 测试类
 */
class ColorUtilTest {

    @Test
    void hexToRgba(){
        System.out.println(ColorUtil.hexToRgba("#6217d7"));
    }

    @Test
    void testHexToRgbaWithStandardColors() {
        // 测试红色
        assertEquals("rgba(255, 0, 0, 1)", ColorUtil.hexToRgba("#FF0000"));
        assertEquals("rgba(255, 0, 0, 1)", ColorUtil.hexToRgba("FF0000"));

        // 测试绿色
        assertEquals("rgba(0, 255, 0, 1)", ColorUtil.hexToRgba("#00FF00"));
        assertEquals("rgba(0, 255, 0, 1)", ColorUtil.hexToRgba("00FF00"));

        // 测试蓝色
        assertEquals("rgba(0, 0, 255, 1)", ColorUtil.hexToRgba("#0000FF"));
        assertEquals("rgba(0, 0, 255, 1)", ColorUtil.hexToRgba("0000FF"));

        // 测试黑色
        assertEquals("rgba(0, 0, 0, 1)", ColorUtil.hexToRgba("#000000"));
        assertEquals("rgba(0, 0, 0, 1)", ColorUtil.hexToRgba("000000"));

        // 测试白色
        assertEquals("rgba(255, 255, 255, 1)", ColorUtil.hexToRgba("#FFFFFF"));
        assertEquals("rgba(255, 255, 255, 1)", ColorUtil.hexToRgba("FFFFFF"));
    }

    @Test
    void testHexToRgbaWithAlphaChannel() {
        // 测试带透明度的红色
        assertEquals("rgba(255, 0, 0, 1.00)", ColorUtil.hexToRgba("#FF0000FF"));
        assertEquals("rgba(255, 0, 0, 0.50)", ColorUtil.hexToRgba("#FF000080"));
        assertEquals("rgba(255, 0, 0, 0.00)", ColorUtil.hexToRgba("#FF000000"));

        // 测试带透明度的绿色
        assertEquals("rgba(0, 255, 0, 1.00)", ColorUtil.hexToRgba("#00FF00FF"));
        assertEquals("rgba(0, 255, 0, 0.50)", ColorUtil.hexToRgba("#00FF0080"));
        assertEquals("rgba(0, 255, 0, 0.00)", ColorUtil.hexToRgba("#00FF0000"));

        // 测试带透明度的蓝色
        assertEquals("rgba(0, 0, 255, 1.00)", ColorUtil.hexToRgba("#0000FFFF"));
        assertEquals("rgba(0, 0, 255, 0.50)", ColorUtil.hexToRgba("#0000FF80"));
        assertEquals("rgba(0, 0, 255, 0.00)", ColorUtil.hexToRgba("#0000FF00"));
    }

    @Test
    void testHexToRgbaWithLowerCase() {
        // 测试小写十六进制字符
        assertEquals("rgba(255, 0, 0, 1)", ColorUtil.hexToRgba("#ff0000"));
        assertEquals("rgba(255, 0, 0, 1)", ColorUtil.hexToRgba("ff0000"));
        assertEquals("rgba(255, 0, 0, 0.50)", ColorUtil.hexToRgba("#ff000080"));
    }

    @Test
    void testHexToRgbaInvalidInput() {
        // 测试空输入
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.hexToRgba(null));
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.hexToRgba(""));

        // 测试无效长度
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.hexToRgba("#FF"));
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.hexToRgba("#FF00"));
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.hexToRgba("#FF00000"));
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.hexToRgba("#FF0000000"));

        // 测试无效字符
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.hexToRgba("#GG0000"));
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.hexToRgba("#FF0000GG"));
    }
}