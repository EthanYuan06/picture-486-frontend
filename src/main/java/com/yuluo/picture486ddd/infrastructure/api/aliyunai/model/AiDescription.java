package com.yuluo.picture486ddd.infrastructure.api.aliyunai.model;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.Constants;
import com.yuluo.picture486ddd.domain.constant.AiPrompt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class AiDescription {
    // 以下为北京地域base_url，若使用弗吉尼亚地域模型，需要将base_url换成 https://dashscope-us.aliyuncs.com/api/v1
    // 若使用新加坡地域的模型，需将base_url替换为：https://dashscope-intl.aliyuncs.com/api/v1
    static {
        Constants.baseHttpApiUrl="https://dashscope.aliyuncs.com/api/v1";}

    public static String callWithLocalFile(String base64Image) throws ApiException, NoApiKeyException, UploadFileException, IOException {
        return callWithBase64(base64Image, "image/png");
    }

    public static String callWithBase64(String base64Image, String mimeType) throws ApiException, NoApiKeyException, UploadFileException, IOException {
        String safeMimeType = (mimeType == null || mimeType.isBlank()) ? "image/png" : mimeType;
        String dataUrl = "data:" + safeMimeType + ";base64," + base64Image;

        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(Arrays.asList(
                        new HashMap<>() {{
                            put("image", dataUrl);
                        }},
                        new HashMap<>() {{
                            put("text", AiPrompt.IMAGE_DESCRIPTION);
                        }}
                )).build();

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                // 各地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .model("qwen3.5-flash")
                .messages(Collections.singletonList(userMessage))
                .build();

        MultiModalConversationResult result = conv.call(param);
        return (String) result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
    }
}
