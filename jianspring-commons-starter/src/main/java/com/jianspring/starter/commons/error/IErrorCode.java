package com.jianspring.starter.commons.error;

import com.jianspring.starter.commons.UserContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;


public interface IErrorCode {

    Logger log = LoggerFactory.getLogger(IErrorCode.class);

    Integer getCode();

    String getDefaultMessage();

    String getBundleName();

    default String getLocalMessage(Object... args) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(getBundleName(), new Locale(UserContextUtils.get().getLocale()));
        String         message;
        if (null == resourceBundle) {
            return formatMessage(getDefaultMessage(), args);
        }
        try {
            message = resourceBundle.getString(String.valueOf(getCode()));
        } catch (Throwable throwable) {
            log.warn("", throwable);
            message = getDefaultMessage();
        }
        return formatMessage(message, args);
    }

    default String formatMessage(String message, Object... args) {
        if (null == args || 0 == args.length) {
            return message;
        }
        try {
            return String.format(message, args);
        } catch (Throwable throwable) {
            log.error("", throwable);
            return message;
        }
    }
}