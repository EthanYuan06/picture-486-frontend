package com.yuluo.picture486ddd.domain.picture.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AiDescriptionTaskMessage implements Serializable {
    private String taskId;
    private int attempt;
}

