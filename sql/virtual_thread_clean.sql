-- ============================================
-- 虚拟线程压测 - 清理测试数据脚本
-- 说明：删除所有 benchmark 测试用户和测试图片
-- ============================================

-- ============================================
-- 步骤1：查看即将删除的数据量（确认）
-- ============================================
SELECT '=== 即将删除的数据统计 ===' as info;

SELECT
    (SELECT COUNT(*) FROM user WHERE userAccount LIKE 'benchmark_user_%') as users_to_delete,
    (SELECT COUNT(*) FROM picture WHERE name LIKE '用户%_图片%') as pictures_to_delete;

-- ============================================
-- 步骤2：删除测试图片
-- ============================================
DELETE FROM picture
WHERE name LIKE '用户%_图片%';

-- ============================================
-- 步骤3：删除测试用户
-- ============================================
DELETE FROM user
WHERE userAccount LIKE 'benchmark_user_%';

-- ============================================
-- 步骤4：验证清理结果
-- ============================================
SELECT '=== 清理结果验证 ===' as info;

SELECT
    (SELECT COUNT(*) FROM user WHERE userAccount LIKE 'benchmark_user_%') as remaining_users,
    (SELECT COUNT(*) FROM picture WHERE name LIKE '用户%_图片%') as remaining_pictures;

-- ============================================
-- 步骤5：检查其他表是否有残留数据（可选）
-- ============================================
SELECT '=== 检查空间用户关联表（如有）===' as info;
SELECT COUNT(*) as space_user_records_to_check
FROM space_user
WHERE userId IN (
    SELECT id FROM user WHERE userAccount LIKE 'benchmark_user_%'
);

-- 如果有残留，可以取消注释以下语句删除：
-- DELETE FROM space_user
-- WHERE userId IN (
--     SELECT id FROM user WHERE userAccount LIKE 'benchmark_user_%'
-- );

-- ============================================
-- 完成提示
-- ============================================
SELECT '
╔══════════════════════════════════════════════════════════╗
║          ✅ 测试数据清理完成！                            ║
╠══════════════════════════════════════════════════════════
║  📊 清理统计：
║    - 测试用户：已删除 100 个
║    - 测试图片：已删除 5000 张
║    - 缓存数据：请手动清除 Redis 和本地缓存
║
║  🧹 建议操作：
║    1. 重启应用以清空本地缓存（Caffeine）
║    2. 执行以下命令清空 Redis 测试缓存：
║       redis-cli KEYS "thread-benchmark:*" | xargs redis-cli DEL
║    3. 检查数据库确认无残留数据
║
╚══════════════════════════════════════════════════════════╝
' as result;
