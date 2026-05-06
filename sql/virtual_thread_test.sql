-- ============================================
-- 虚拟线程压测 - 批量插入测试数据脚本
-- 方案：生成100个测试用户，每个用户上传50张相同图片
-- 总计：100用户 × 50图片 = 5000张图片
-- 图片URL：https://yuluo-picture-1383397986.cos.ap-guangzhou.myqcloud.com/public/1985558297927299073/2026-03-09_BYla3G3X6tlEs2UD.webp
-- 使用说明：执行一次即可生成所有测试数据
-- ============================================

-- 设置变量
SET @test_image_url = 'https://yuluo-picture-1383397986.cos.ap-guangzhou.myqcloud.com/public/1985558297927299073/2026-03-09_BYla3G3X6tlEs2UD.webp';
SET @test_thumbnail_url = 'https://yuluo-picture-1383397986.cos.ap-guangzhou.myqcloud.com/public/1985558297927299073/2026-03-09_BYla3G3X6tlEs2UD.webp';
SET @pictures_per_user = 50;
SET @total_users = 100;

-- ============================================
-- 步骤1：批量插入100个测试用户
-- ============================================
INSERT INTO user (userAccount, userEmail, userPassword, userName, userAvatar, userProfile, userRole, createTime, editTime, updateTime, isDelete)
SELECT
    CONCAT('benchmark_user_', seq),
    CONCAT('benchmark_', seq, '@test.com'),
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
    CONCAT('压测用户', seq),
    'https://yuluo-picture-1383397986.cos.ap-guangzhou.myqcloud.com/default_avatar.png',
    CONCAT('虚拟线程压测用户', seq),
    'user',
    NOW(),
    NOW(),
    NOW(),
    0
FROM (
    SELECT @row := @row + 1 as seq
    FROM (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
          UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t1,
         (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
          UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t2,
         (SELECT @row := 0) r
) numbers
WHERE seq <= @total_users
ON DUPLICATE KEY UPDATE
    userName = VALUES(userName),
    userProfile = VALUES(userProfile),
    editTime = NOW();

-- ============================================
-- 步骤2：批量插入5000张图片（每个用户50张）
-- ============================================
INSERT INTO picture (
    url,
    thumbnailUrl,
    name,
    introduction,
    category,
    tags,
    picSize,
    picWidth,
    picHeight,
    picScale,
    picFormat,
    userId,
    spaceId,
    reviewStatus,
    reviewMessage,
    reviewerId,
    reviewTime,
    createTime,
    editTime,
    updateTime,
    isDelete
)
SELECT
    @test_image_url,
    @test_thumbnail_url,
    CONCAT('用户', user_seq, '_图片', pic_seq),
    CONCAT('虚拟线程压测图片 - 用户', user_seq, '的第', pic_seq, '张图片'),
    'benchmark',
    '["压测", "虚拟线程测试", "webp"]',
    53488,
    1080,
    1067,
    1.01,
    'WEBP',
    u.id,
    NULL,
    1,
    '管理员自动过审',
    u.id,
    NOW(),
    DATE_SUB(NOW(), INTERVAL (user_seq * 10 + pic_seq) MINUTE),
    NOW(),
    NOW(),
    0
FROM (
    SELECT @user_row := @user_row + 1 as user_seq
    FROM (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
          UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t1,
         (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
          UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t2,
         (SELECT @user_row := 0) r
) users
CROSS JOIN (
    SELECT @pic_row := @pic_row + 1 as pic_seq
    FROM (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
          UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t1,
         (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
          UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t2,
         (SELECT @pic_row := 0) r
) pics
INNER JOIN user u ON u.userAccount = CONCAT('benchmark_user_', users.user_seq)
WHERE users.user_seq <= @total_users
  AND pics.pic_seq <= @pictures_per_user;

-- ============================================
-- 步骤3：数据验证
-- ============================================

-- 查看用户总数
SELECT '=== 测试用户统计 ===' as info;
SELECT
    COUNT(*) as user_count,
    MIN(id) as min_user_id,
    MAX(id) as max_user_id
FROM user
WHERE userAccount LIKE 'benchmark_user_%';

-- 查看图片总数
SELECT '=== 测试图片统计 ===' as info;
SELECT
    COUNT(*) as picture_count,
    COUNT(DISTINCT userId) as unique_users,
    MIN(userId) as min_user_id,
    MAX(userId) as max_user_id
FROM picture
WHERE name LIKE '用户%_图片%';

-- 查看每个用户的图片数量（应该都是50张）
SELECT '=== 每个用户的图片数量 ===' as info;
SELECT
    u.userAccount,
    u.userName,
    COUNT(p.id) AS picture_count
FROM user u
LEFT JOIN picture p ON u.id = p.userId AND p.name LIKE '用户%_图片%'
WHERE u.userAccount LIKE 'benchmark_user_%'
GROUP BY u.id, u.userAccount, u.userName
ORDER BY u.id
LIMIT 20;

-- 查看图片详细信息示例
SELECT '=== 图片详细信息示例（前10条）===' as info;
SELECT
    id,
    name,
    url,
    userId,
    picFormat,
    picSize,
    createTime
FROM picture
WHERE name LIKE '用户%_图片%'
ORDER BY userId, name
LIMIT 10;

-- 统计信息汇总
SELECT '=== 数据完整性检查 ===' as info;
SELECT
    (SELECT COUNT(*) FROM user WHERE userAccount LIKE 'benchmark_user_%') as total_users,
    (SELECT COUNT(*) FROM picture WHERE name LIKE '用户%_图片%') as total_pictures,
    (SELECT COUNT(DISTINCT userId) FROM picture WHERE name LIKE '用户%_图片%') as users_with_pictures,
    (SELECT MIN(picture_count) FROM (
        SELECT COUNT(*) as picture_count
        FROM picture
        WHERE name LIKE '用户%_图片%'
        GROUP BY userId
    ) t) as min_pictures_per_user,
    (SELECT MAX(picture_count) FROM (
        SELECT COUNT(*) as picture_count
        FROM picture
        WHERE name LIKE '用户%_图片%'
        GROUP BY userId
    ) t) as max_pictures_per_user;

-- ============================================
-- 完成提示
-- ============================================
SELECT '
╔══════════════════════════════════════════════════════════╗
║          ✅ 虚拟线程压测数据生成完成！                     ║
╠══════════════════════════════════════════════════════════
║  📊 数据统计：
║    - 测试用户：100个（benchmark_user_1 ~ benchmark_user_100）  ║
║    - 测试图片：5000张（每个用户50张）
║    - 图片格式：WEBP                                      ║
║    - 图片大小：53KB（与您提供的图片一致）                   ║
║    - 图片尺寸：1080×1067                                  ║
╠══════════════════════════════════════════════════════════╣
║  🚀 下一步操作：                                         ║
║    1. 启动应用                                           ║
║    2. 验证虚拟线程：curl http://localhost:8123/api/virtual-thread/check  ║
║    3. 执行压测：使用JMeter或压测接口                       ║
╠══════════════════════════════════════════════════════════╣
║  🧹 清理数据：
║    DELETE FROM picture WHERE name LIKE "用户%_图片%";     ║
║    DELETE FROM user WHERE userAccount LIKE "benchmark_user_%";  ║
╚══════════════════════════════════════════════════════════╝
' as result;
