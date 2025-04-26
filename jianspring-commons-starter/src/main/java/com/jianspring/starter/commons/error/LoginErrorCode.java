package com.jianspring.starter.commons.error;

public enum LoginErrorCode implements IErrorCode {
    // 定义常见的登录错误
    ACCOUNT_NOT_FOUND(1000, "用户不存在"),
    USERNAME_OR_PASSWORD_ERROR(1001, "用户名或密码错误"),
    ACCOUNT_LOCKED(1002, "账户已锁定"),
    TOO_MANY_ATTEMPTS(1003, "登录错误次数过多，请稍后再试"),
    INVALID_TOKEN(1004, "无效的Token"),
    EXPIRED_TOKEN(1005, "Token已过期"),
    
    // 新增的登录异常
    CAPTCHA_EMPTY(1006, "验证码不能为空"),
    UNSUPPORTED_VERIFICATION_TYPE(1007, "不支持的验证类型"),
    USER_INFO_NOT_EXIST(1008, "用户信息不存在"),
    ;

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
