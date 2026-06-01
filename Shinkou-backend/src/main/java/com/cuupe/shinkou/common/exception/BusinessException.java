package com.cuupe.shinkou.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
    private final String code;

    /**
     * 构造业务异常
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}
