package com.yuluo.picture486backend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuluo.picture486backend.exception.BusinessException;
import com.yuluo.picture486backend.exception.ErrorCode;
import com.yuluo.picture486backend.exception.ThrowUtils;
import com.yuluo.picture486backend.model.dto.space.analyze.*;
import com.yuluo.picture486backend.model.entity.Picture;
import com.yuluo.picture486backend.model.entity.Space;
import com.yuluo.picture486backend.model.entity.User;
import com.yuluo.picture486backend.model.vo.space.analyze.*;
import com.yuluo.picture486backend.service.PictureService;
import com.yuluo.picture486backend.service.SpaceAnalyzeService;
import com.yuluo.picture486backend.service.SpaceService;
import com.yuluo.picture486backend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class SpaceAnalyzeServiceImpl implements SpaceAnalyzeService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private PictureService pictureService;

    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
        //参数校验
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        if (spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()) {
            //查询全部相册或公共图库
            //仅管理员访问
            boolean isAdmin = userService.isAdmin(loginUser);
            ThrowUtils.throwIf(!isAdmin, ErrorCode.NO_AUTH_ERROR, "无分析权限");
            //统计公共图库资源使用
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            //若查询公共图库，则查询space == null的图片
            if (spaceUsageAnalyzeRequest.isQueryPublic()) {
                queryWrapper.isNull("spaceId");
            }
            //若查询全相册，则查询space != null的图片
            if (spaceUsageAnalyzeRequest.isQueryAll()) {
                queryWrapper.isNotNull("spaceId");
            }
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long usedSize = pictureObjList.stream().mapToLong(result -> result instanceof Long ? (Long) result : 0).sum();
            long usedCount = pictureObjList.size();
            //封装返回结果
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            //公共图库无上限、无比例，不作设置
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);
            return spaceUsageAnalyzeResponse;
        } else {
            //查询指定相册
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            //获取相册信息
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "相册不存在");
            //权限校验：仅相册创建人或管理员访问
            spaceService.checkSpaceAuth(space, loginUser);
            //构造返回结果
            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            response.setUsedSize(space.getTotalSize());
            response.setMaxSize(space.getMaxSize());
            //计算百分比，让前端直接展示
            double sizeUsageRatio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            response.setSizeUsageRatio(sizeUsageRatio);
            response.setUsedCount(space.getTotalCount());
            response.setMaxCount(space.getMaxCount());
            double countUsageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            response.setCountUsageRatio(countUsageRatio);
            return response;
        }
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //检查权限
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        //构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        //根据分析范围补充查询条件
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        //分组查询。按category字段分组，统计每类图片的数量和总大小
        queryWrapper.select("category AS category", "count(*) AS count", "sum(picSize) AS totalSize")
                .groupBy("category");
        //转换结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(result -> {
                    String category = result.get("category") != null ? result.get("category").toString() : "未分类";
                    //由于result.get("count")返回的是Object类型（实际运行时是Number的某个子类）
                    //需要先将其转换为Number类型，然后再调用longValue()方法获取long值，避免类型转换异常
                    Long count = ((Number)result.get("count")).longValue();
                    Long totalSize = ((Number)result.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
                }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        //检查权限
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
        //构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        //根据分析范围补充查询条件
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        //查询所有符合条件的标签
        queryWrapper.select("tags");
        List<String> tagsJsonList =
                pictureService.getBaseMapper().selectMaps(queryWrapper)//根据tags字段查询所有数据
                .stream().filter(ObjUtil::isNotNull)//只筛选tags不为null的数据
                .map(Objects::toString)//对以上数据逐个转换为字符串，转换的结果是JSON字符串
                .toList();//处理结果以集合返回JSON字符串
        //合并所有标签并统计使用次数
        Map<String, Long> tagCountMap = tagsJsonList.stream()
                .flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream())//将多个流合并成一个流，扁平化处理
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));//按标签本身的内容分组（tag -> tag），统计每个标签出现的次数
        //转换为响应对象，按使用次数降序排序
        return tagCountMap.entrySet().stream()//entrySet()方法返回Map中所有键值对的集合视图
                //降序排序，如果e2 > e1，返回正数，根据comparator，返回正数即让e2排在e1前面，实现降序。不填写默认升序
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                //这里对每一个键值对转换为响应对象，为了更方便地填充键和值，上面使用entrySet()转换tagCountMap，这里才能直接调用entry.getKey()和entry.getValue()
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        //检查权限
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        //构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        //根据分析范围补充查询条件
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);
        //查询所有符合条件的图片大小
        queryWrapper.select("picSize");
        List<Long> picSizes =
                pictureService.getBaseMapper().selectMaps(queryWrapper)
                        .stream().filter(ObjUtil::isNotNull)
                        .map(size ->((Number)size).longValue())
                        .toList();
        //定义分段范围，使用有序的Map
        Map<String, Long> sizeRanges = new TreeMap<>();
        sizeRanges.put("< 1MB", picSizes.stream().filter(size -> size < 1024 * 1024).count());
        sizeRanges.put("1MB-5MB", picSizes.stream().filter(size -> size >= 1024 * 1024 && size < 5 * 1024 * 1024).count());
        sizeRanges.put("5MB-10MB", picSizes.stream().filter(size -> size >= 5 * 1024 * 1024 && size < 10 * 1024 * 1024).count());
        sizeRanges.put("10MB-15MB", picSizes.stream().filter(size -> size >= 10 * 1024 * 1024 && size < 15 * 1024 * 1024).count());

        //转换为响应对象
        return sizeRanges.entrySet().stream().
                map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        //检查权限
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        //构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        //根据分析范围补充查询条件
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);
        //分析维度：日、周、月（使用Java14的switch语法）
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day" -> queryWrapper.select("date_format(createTime, '%Y-%m-%d') as period", "count(*) as count");
            case "week" -> queryWrapper.select("yearweek(createTime) as period", "count(*) as count");
            case "month" -> queryWrapper.select("date_format(createTime, '%Y-%m') as period", "count(*) as count");
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "时间维度不存在");
        }
        //分组和排序
        queryWrapper.groupBy("period").orderByAsc("period");
        //查询结果并转换
        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream().map(result -> {
                    String period = result.get("period").toString();
                    Long count = ((Number) result.get("count")).longValue();
                    return new SpaceUserAnalyzeResponse(period, count);
                }).toList();
    }

    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //仅管理员可查看相册排行
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "没有权限查看相册使用量排行");
        //构造查询条件
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "userId", "totalSize")
                .orderByDesc("totalSize")
                .last("limit " + spaceRankAnalyzeRequest.getTopN());
        //查询结果
        return spaceService.list(queryWrapper);
    }

    /**
     * 检查相册分析权限
     * @param spaceAnalyzeRequest 相册分析请求
     * @param loginUser 当前登录用户
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser){
        //检查权限
        if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()){
            //全相册分析或公共图库的权限校验：仅管理员访问
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无公共图库访问权限");
        } else {
            //私有相册权限校验：仅相册创建人与管理员访问
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "相册不存在");
            spaceService.checkSpaceAuth(space, loginUser);
        }
    }

    /**
     * 填充相册分析查询条件
     * @param spaceAnalyzeRequest 相册分析请求
     * @param queryWrapper 查询条件
     */
    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper){
        //如果查询所有的相册，不拼接条件，返回
        if (spaceAnalyzeRequest.isQueryAll()){
            return;
        }
        //如果查询公共图库，拼接spaceId = null，返回
        if (spaceAnalyzeRequest.isQueryPublic()){
            queryWrapper.isNull("spaceId");
            return;
        }
        //如果查询私有相册，拼接spaceId = spaceId，返回
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceAnalyzeRequest.getSpaceId() != null){
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }



}
