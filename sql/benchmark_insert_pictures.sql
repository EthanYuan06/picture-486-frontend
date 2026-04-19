-- ============================================
-- 图片列表查询压测 - 批量插入测试数据脚本
-- 方案：生成100个测试用户，每个用户上传50张相同图片
-- 总计：100用户 × 50图片 = 5000张图片
-- 使用说明：执行一次即可生成所有测试数据
-- ============================================

-- 设置变量
SET @test_image_url = 'https://yuluo-picture-1383397986.cos.ap-guangzhou.myqcloud.com/public/1985558297927299073/2026-01-07_YYNASfOrdsCRN65U_thumbnail.jpeg';
SET @pictures_per_user = 50; -- 每个用户上传的图片数量
SET @total_users = 100; -- 测试用户数量
SET @total_pictures = 5000; -- 总图片数量

-- ============================================
-- 第一步：批量生成100个测试用户
-- ============================================
INSERT INTO user (
    userAccount,
    userEmail,
    userPassword,
    userName,
    userAvatar,
    userProfile,
    userRole,
    createTime,
    editTime,
    updateTime,
    isDelete
)
SELECT 
    CONCAT('benchmark_user_', u.num) AS userAccount,
    CONCAT('benchmark', u.num, '@test.com') AS userEmail,
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi' AS userPassword, -- 加密后的密码: Test@1234
    CONCAT('压测用户', u.num) AS userName,
    CONCAT('https://example.com/avatar', (u.num % 10), '.jpg') AS userAvatar,
    CONCAT('这是第', u.num, '个压测测试用户') AS userProfile,
    'user' AS userRole,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY) AS createTime,
    NOW() AS editTime,
    NOW() AS updateTime,
    0 AS isDelete
FROM (
    -- 生成1到100的序列号
    SELECT @user_row := @user_row + 1 AS num
    FROM 
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t1,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t2,
        (SELECT @user_row := 0) r
    LIMIT 100
) u;

-- 查看用户插入结果
SELECT 
    COUNT(*) AS total_users_created,
    MIN(id) AS min_user_id,
    MAX(id) AS max_user_id,
    MIN(userAccount) AS first_account,
    MAX(userAccount) AS last_account
FROM user 
WHERE userAccount LIKE 'benchmark_user_%';

-- ============================================
-- 第二步：为每个测试用户生成50张图片（共5000张）
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
    createTime, 
    editTime, 
    updateTime, 
    reviewStatus, 
    reviewMessage, 
    reviewerId, 
    reviewTime, 
    isDelete
)
SELECT 
    -- 图片URL（所有图片使用同一个URL）
    @test_image_url AS url,
    
    -- 缩略图URL
    @test_image_url AS thumbnailUrl,
    
    -- 图片名称（格式：用户X_图片Y）
    CONCAT('用户', user_info.user_num, '_图片', pic_seq.pic_num) AS name,
    
    -- 简介
    CONCAT('压测用户', user_info.user_num, '上传的第', pic_seq.pic_num, '张图片') AS introduction,
    
    -- 分类（随机分配几个分类）
    ELT(FLOOR(1 + RAND() * 4), '素材', '表情包', '壁纸', '二次元') AS category,
    
    -- 标签（JSON数组）
    '["压测","性能测试"]' AS tags,
    
    -- 图片大小（随机100KB-5MB）
    FLOOR(102400 + RAND() * 5142400) AS picSize,
    
    -- 图片宽度
    1920 AS picWidth,
    
    -- 图片高度
    1080 AS picHeight,
    
    -- 宽高比
    1.78 AS picScale,
    
    -- 图片格式
    'jpeg' AS picFormat,
    
    -- 用户ID（从刚创建的测试用户中获取）
    user_info.user_id AS userId,
    
    -- 空间ID（NULL表示公共图库）
    NULL AS spaceId,
    
    -- 创建时间（随机分布在最近30天内，同一用户的图片时间相近）
    DATE_SUB(NOW(), INTERVAL (FLOOR(RAND() * 30) + (user_info.user_num % 5)) DAY) AS createTime,
    
    -- 编辑时间
    NOW() AS editTime,
    
    -- 更新时间
    NOW() AS updateTime,
    
    -- 审核状态（1-已通过）
    1 AS reviewStatus,
    
    -- 审核信息
    '压测数据自动通过' AS reviewMessage,
    
    -- 审核人ID（使用第一个测试用户作为审核人）
    (SELECT MIN(id) FROM user WHERE userAccount LIKE 'benchmark_user_%') AS reviewerId,
    
    -- 审核时间
    NOW() AS reviewTime,
    
    -- 是否删除（0-未删除）
    0 AS isDelete

FROM (
    -- 生成100个测试用户ID和序号
    SELECT 
        @user_row := @user_row + 1 AS user_num,
        u.id AS user_id
    FROM user u,
        (SELECT @user_row := 0) r
    WHERE u.userAccount LIKE 'benchmark_user_%'
    ORDER BY u.id
    LIMIT 100
) AS user_info

CROSS JOIN (
    -- 生成1到50的序号（每个用户的50张图片）
    SELECT @pic_row := @pic_row + 1 AS pic_num
    FROM 
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t1,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t2,
        (SELECT @pic_row := 0) r
    LIMIT 50
) AS pic_seq

ORDER BY user_info.user_num, pic_seq.pic_num;

-- ============================================
-- 第三步：验证数据插入结果
-- ============================================

-- 查看测试用户统计
SELECT 
    COUNT(*) AS total_test_users,
    MIN(id) AS min_user_id,
    MAX(id) AS max_user_id
FROM user 
WHERE userAccount LIKE 'benchmark_user_%';

-- 查看测试图片统计
SELECT 
    COUNT(*) AS total_test_pictures,
    COUNT(DISTINCT userId) AS unique_users,
    MIN(userId) AS min_user_id,
    MAX(userId) AS max_user_id,
    MIN(name) AS first_picture_name,
    MAX(name) AS last_picture_name,
    MIN(createTime) AS earliest_create_time,
    MAX(createTime) AS latest_create_time
FROM picture 
WHERE name LIKE '用户%_图片%';

-- 查看每个用户的图片数量（应该都是50张）
SELECT 
    u.userAccount,
    u.userName,
    COUNT(p.id) AS picture_count
FROM user u
LEFT JOIN picture p ON u.id = p.userId
WHERE u.userAccount LIKE 'benchmark_user_%'
GROUP BY u.id, u.userAccount, u.userName
ORDER BY u.id
LIMIT 20; -- 只显示前20个用户

-- ============================================
-- 完成提示
-- ============================================
-- ✅ 测试数据生成完成！
-- 📊 数据统计：
--   - 测试用户：100个（benchmark_user_1 ~ benchmark_user_100）
--   - 测试图片：5000张（每个用户50张）
--   - 图片分布：100用户 × 50图片 = 5000张
-- 
-- 🔍 验证方法：
--   1. 检查用户数量：SELECT COUNT(*) FROM user WHERE userAccount LIKE 'benchmark_user_%';
--   2. 检查图片数量：SELECT COUNT(*) FROM picture WHERE name LIKE '用户%_图片%';
--   3. 检查每个用户的图片数：SELECT userId, COUNT(*) FROM picture WHERE name LIKE '用户%_图片%' GROUP BY userId;
--
-- 🚀 下一步：
--   1. 启动应用
--   2. 访问压测接口：http://localhost:8080/picture/benchmark/compare?current=1&size=20
--   3. 查看性能对比结果
--
-- 🧹 清理数据：
--   执行 sql/benchmark_cleanup.sql 清理所有测试数据
-- ============================================
