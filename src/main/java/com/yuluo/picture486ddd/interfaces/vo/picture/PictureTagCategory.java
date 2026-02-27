package com.yuluo.picture486ddd.interfaces.vo.picture;

import lombok.Data;

import java.util.List;

@Data
public class PictureTagCategory {
    /**
     * 标签
     */
    private List<String> tagList;

    /**
     * 分类
     */
    private List<String> categoryList;
}
