package com.yuluo.picture486ddd.infrastructure.manager.sharding;

import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.yuluo.picture486backend.model.entity.Space;
import com.yuluo.picture486backend.model.enums.SpaceLevelEnum;
import com.yuluo.picture486backend.model.enums.SpaceTypeEnum;
import com.yuluo.picture486backend.service.SpaceService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分表管理器类
 * 解决框架本身不支持动态维护分表的问题
 */
//@Component
@Slf4j
public class DynamicShardingManager {

    @Resource
    private DataSource dataSource;

    @Resource
    private SpaceService spaceService;

    private static final String LOGIC_TABLE_NAME = "picture";

    private static final String DATABASE_NAME = "486_picture"; // 配置文件中的数据库名称

    @PostConstruct
    public void initialize() {
        log.info("开始初始化动态分表配置...");
        log.info("DataSource 类型: {}", dataSource.getClass().getName());
        
        // 检查是否支持 ShardingSphere 相关操作
        try {
            updateShardingTableNodes();
            log.info("动态分表配置初始化完成");
        } catch (Exception e) {
            log.error("动态分表配置初始化失败: {}", e.getMessage());
            log.warn("动态分表功能暂时不可用，应用将继续运行");
            // 不抛出异常，避免影响应用启动
        }
    }

    public void createSpacePictureTable(Space space) {
        // 动态创建分表
        // 仅为旗舰版团队相册创建分表
        if (space.getSpaceType() == SpaceTypeEnum.TEAM.getValue() && space.getSpaceLevel() == SpaceLevelEnum.FLAGSHIP.getValue()) {
            Long spaceId = space.getId();
            String tableName = "picture_" + spaceId;
            // 创建新表
            String createTableSql = "CREATE TABLE " + tableName + " LIKE picture";
            try {
                SqlRunner.db().update(createTableSql);
                // 更新分表
                updateShardingTableNodes();
            } catch (Exception e) {
                log.error("创建图片相册分表失败，相册 id = {}", space.getId());
            }
        }
    }

    /**
     * 获取所有动态表名，包括初始表 picture 和分表 picture_{spaceId}
     */
    private Set<String> fetchAllPictureTableNames() {
        // 为了测试方便，直接对所有团队相册分表（实际上线改为仅对旗舰版生效）
        Set<Long> spaceIds = spaceService.lambdaQuery()
                .eq(Space::getSpaceType, SpaceTypeEnum.TEAM.getValue())
                .list()
                .stream()
                .map(Space::getId)
                .collect(Collectors.toSet());
        Set<String> tableNames = spaceIds.stream()
                .map(spaceId -> LOGIC_TABLE_NAME + "_" + spaceId)
                .collect(Collectors.toSet());
        tableNames.add(LOGIC_TABLE_NAME); // 添加初始逻辑表
        return tableNames;
    }

    /**
     * 更新 ShardingSphere 的 actual-data-nodes 动态表名配置
     */
    private void updateShardingTableNodes() {
        Set<String> tableNames = fetchAllPictureTableNames();
        String newActualDataNodes = tableNames.stream()
                .map(tableName -> "486_picture." + tableName) // 确保前缀合法
                .collect(Collectors.joining(","));
        log.info("动态分表 actual-data-nodes 配置: {}", newActualDataNodes);

        ContextManager contextManager = getContextManager();
        ShardingSphereRuleMetaData ruleMetaData = contextManager.getMetaDataContexts()
                .getMetaData()
                .getDatabases()
                .get(DATABASE_NAME)
                .getRuleMetaData();

        Optional<ShardingRule> shardingRule = ruleMetaData.findSingleRule(ShardingRule.class);
        if (shardingRule.isPresent()) {
            ShardingRuleConfiguration ruleConfig = (ShardingRuleConfiguration) shardingRule.get().getConfiguration();
            List<ShardingTableRuleConfiguration> updatedRules = ruleConfig.getTables()
                    .stream()
                    .map(oldTableRule -> {
                        if (LOGIC_TABLE_NAME.equals(oldTableRule.getLogicTable())) {
                            ShardingTableRuleConfiguration newTableRuleConfig = new ShardingTableRuleConfiguration(LOGIC_TABLE_NAME, newActualDataNodes);
                            newTableRuleConfig.setDatabaseShardingStrategy(oldTableRule.getDatabaseShardingStrategy());
                            newTableRuleConfig.setTableShardingStrategy(oldTableRule.getTableShardingStrategy());
                            newTableRuleConfig.setKeyGenerateStrategy(oldTableRule.getKeyGenerateStrategy());
                            newTableRuleConfig.setAuditStrategy(oldTableRule.getAuditStrategy());
                            return newTableRuleConfig;
                        }
                        return oldTableRule;
                    })
                    .collect(Collectors.toList());
            ruleConfig.setTables(updatedRules);
            contextManager.alterRuleConfiguration(DATABASE_NAME, Collections.singleton(ruleConfig));
            contextManager.reloadDatabase(DATABASE_NAME);
            log.info("动态分表规则更新成功！");
        } else {
            log.error("未找到 ShardingSphere 的分片规则配置，动态分表更新失败。");
        }
    }

    /**
     * 获取 ShardingSphere ContextManager
     */
    private ContextManager getContextManager() {
        // 首先尝试通过连接 unwrap 获取
        try (ShardingSphereConnection connection = dataSource.getConnection().unwrap(ShardingSphereConnection.class)) {
            return connection.getContextManager();
        } catch (SQLException e) {
            log.debug("通过连接 unwrap 获取 ContextManager 失败: {}", e.getMessage());
        }
        
        // 备用方案：通过反射获取
        try {
            // 检查是否可以通过反射获取 ContextManager
            if (dataSource.getClass().getName().contains("ShardingSphere")) {
                // 尝试通过反射调用 getContextManager 方法
                java.lang.reflect.Method method = dataSource.getClass().getMethod("getContextManager");
                return (ContextManager) method.invoke(dataSource);
            }
        } catch (Exception e) {
            log.debug("通过反射获取 ContextManager 失败: {}", e.getMessage());
        }
        
        throw new RuntimeException("无法获取 ShardingSphere ContextManager，当前数据源类型: " + dataSource.getClass().getName());
    }
}
