-- ============================================
-- 清理压测数据脚本
-- 删除所有测试用户和测试图片
-- ============================================

-- 第一步：查看将要删除的数据量
SELECT '=== 即将删除的测试用户 ===' AS info;
SELECT 
    COUNT(*) AS will_delete_users,
    MIN(id) AS min_user_id,
    MAX(id) AS max_user_id,
    MIN(userAccount) AS first_account,
    MAX(userAccount) AS last_account
FROM user 
WHERE userAccount LIKE 'benchmark_user_%';

SELECT '=== 即将删除的测试图片 ===' AS info;
SELECT 
    COUNT(*) AS will_delete_pictures,
    COUNT(DISTINCT userId) AS affected_users,
    MIN(id) AS min_picture_id,
    MAX(id) AS max_picture_id
FROM picture 
WHERE name LIKE '用户%_图片%';

-- 第二步：确认无误后，执行删除操作

-- 先删除测试图片（因为有外键依赖）
DELETE FROM picture 
WHERE name LIKE '用户%_图片%';

-- 再删除测试用户
DELETE FROM user 
WHERE userAccount LIKE 'benchmark_user_%';

-- 第三步：验证删除结果
SELECT '=== 删除后的验证 ===' AS info;

SELECT 
    COUNT(*) AS remaining_users
FROM user 
WHERE userAccount LIKE 'benchmark_user_%';

SELECT 
    COUNT(*) AS remaining_pictures
FROM picture 
WHERE name LIKE '用户%_图片%';

-- 完成提示
SELECT '✅ 测试数据清理完成！' AS result;
