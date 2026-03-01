package com.yuluo.picture486ddd.domain.constant;

public interface AiPrompt {
    String IMAGE_DESCRIPTION =
            "请分析这张图片，生成一段适合社交媒体发布的简介。要求：" +
                    "1. 语气自然亲切，但不要让人感到过于语气化，稍微正式一些" +
                    "2. 可以适当添加相关的 emoji 表情丰富文案" +
                    "3. 直接输出简介内容，无需额外说明" +
                    "4. 保持简洁，一段话30字即可";
}
