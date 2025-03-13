package com.jianspring.starter.commons.error;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum CommonErrorCode implements IErrorCode {

    SUCCESS(200, "Success"),
    ERROR(500, "Error"),
    BIZ_ERROR(501, "Biz Error"),
    SYSTEM_ERROR(502, "System Error"),
    PARAM_ERROR(400, "Param Error"),
    NOT_LOGIN_IN(401, "Not logged in"),
    DATA_PARSE_ERROR(402, "Param Parse Error"),
    HAVE_ON_AUTHORITY(403, "Have On Authority"),
    HTTP_METHOD_NOT_SUPPORT(405, "Http Method : %s Not Support"),
    ILLEGAL_TOKEN(406, "illegal Token"),
    EXCEED_MAX_SESSION(407, "exceed max session"),
    OVER_FILE_SIZE(413, "update over file size"),
    TOKEN_NOT_EXIST(409, "token not exist"),
    ILLEGAL_REQUEST(503, "Illegal request")
    ;

    private final Integer code;

    private final String defaultMessage;

    CommonErrorCode(Integer code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getBundleName() {
        return "commons";
    }
}