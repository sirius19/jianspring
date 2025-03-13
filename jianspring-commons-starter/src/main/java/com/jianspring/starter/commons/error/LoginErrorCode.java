package com.jianspring.starter.commons.error;

public enum LoginErrorCode implements IErrorCode {
    // 定义常见的登录错误
    USERNAME_OR_PASSWORD_ERROR(1001, "用户名或密码错误"),
    ACCOUNT_LOCKED(1002, "账户已锁定"),
    TOO_MANY_ATTEMPTS(1003, "登录错误次数过多，请稍后再试"),
    INVALID_TOKEN(1004, "无效的Token"),
    EXPIRED_TOKEN(1005, "Token已过期");

    private final Integer code;
    private final String defaultMessage;

    private LoginErrorCode(Integer code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getDefaultMessage() {
        return this.defaultMessage;
    }

    @Override
    public String getBundleName() {
        return "login_messages";
    }
}
