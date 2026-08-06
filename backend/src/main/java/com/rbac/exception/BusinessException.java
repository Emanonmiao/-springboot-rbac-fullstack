package com.rbac.exception;

import com.rbac.common.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 * 业务逻辑中抛出此异常，由GlobalExceptionHandler统一捕获处理
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
