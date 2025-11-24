package com.yuluo.picture486backend.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuluo.picture486backend.model.entity.Picture;
import com.yuluo.picture486backend.service.PictureService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class PictureCleanupScheduler {

    @Resource
    private PictureService pictureService;

    /**
     * 每一天执行一次物理删除 isDelete=1 的数据
     * cron表达式: 秒 分 时 日 月 周
     * 0 0 2 * * ? 表示每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupDeletedPictures() {
        log.info("开始执行图片物理删除任务");

        try {
            // 计算1天前的时间
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            Date thirtyDaysAgoDate = Date.from(oneDayAgo.atZone(ZoneId.systemDefault()).toInstant());

            // 查询 isDelete=1 且 updateTime 在30天前的数据
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_delete", 1)
                    .lt("update_time", thirtyDaysAgoDate);

            List<Picture> picturesToDelete = pictureService.list(queryWrapper);

            if (picturesToDelete.isEmpty()) {
                log.info("没有找到需要物理删除的图片数据");
                return;
            }

            log.info("找到 {} 条需要物理删除的图片数据", picturesToDelete.size());

            // 物理删除这些数据
            for (Picture picture : picturesToDelete) {
                // 先清理存储桶图片文件
                pictureService.clearPictureFile(picture);

                // 从数据库中物理删除记录
                boolean removed = pictureService.removeById(picture.getId());
                if (removed) {
                    log.info("成功物理删除图片，ID: {}", picture.getId());
                } else {
                    log.warn("物理删除图片失败，ID: {}", picture.getId());
                }
            }

            log.info("图片物理删除任务执行完成");
        } catch (Exception e) {
            log.error("执行图片物理删除任务时发生错误", e);
        }
    }
}