package com.yuluo.picture486backend.scheduler;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuluo.picture486backend.model.entity.Picture;
import com.yuluo.picture486backend.service.PictureService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

@SpringBootTest
class PictureCleanupSchedulerTest {

    @Resource
    private PictureCleanupScheduler pictureCleanupScheduler;

    // 使用 @MockitoBean 注解来模拟 PictureService，防止在测试环境中真正删除文件
    @MockitoBean
    private PictureService pictureService;

    @Test
    void manualCleanupDeletedPictures() {
        // 手动执行物理删除任务，便于测试和手动触发
        pictureCleanupScheduler.cleanupDeletedPictures();
    }
    @Test
    void testTimeFormat(){
        List<Picture> list = pictureService.list(new QueryWrapper<Picture>().eq("isDelete", 1));
        list.forEach(p -> System.out.println("Java 中读取的时间: " + p.getUpdateTime()));
    }
}