package com.jianspring.starter.iam;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jianspring.start.utils.lambda.GenericEntityBuilder;
import com.jianspring.starter.commons.UserContextUtils;

import java.util.Map;

/**
 * @Author: InfoInsights
 * @date 2023/4/27 9:37
 */

public class IamJwtService {

    private IamJwtProperties iamJwtProperties;

    public IamJwtService(IamJwtProperties iamJwtProperties) {
        this.iamJwtProperties = iamJwtProperties;
    }

    public UserContextUtils.UserContext getLoginBaseInfo(String token) {
        if (StrUtil.isEmpty(token) || StrUtil.isEmpty(iamJwtProperties.getSign())) {
            return null;
        }
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(iamJwtProperties.getSign()))
        .withIssuer("jian-spring")            
        .build()
        .verify(token);
        Map<String, Claim> claims = decodedJWT.getClaims();
        return GenericEntityBuilder.of(UserContextUtils.UserContext::new)
                .with(UserContextUtils.UserContext::setAccountId, getLong(claims, UserContextUtils.UserContext::getAccountId))
                .with(UserContextUtils.UserContext::setUserId, getLong(claims, UserContextUtils.UserContext::getUserId))
                .with(UserContextUtils.UserContext::setUserType, getInt(claims, UserContextUtils.UserContext::getUserType))
                .with(UserContextUtils.UserContext::setMobileNumber, getString(claims, UserContextUtils.UserContext::getMobileNumber))
                .with(UserContextUtils.UserContext::setName, getString(claims, UserContextUtils.UserContext::getName))
                .with(UserContextUtils.UserContext::setUserName, getString(claims, UserContextUtils.UserContext::getUserName))
                .with(UserContextUtils.UserContext::setTenantId, getLong(claims, UserContextUtils.UserContext::getTenantId))
                .with(UserContextUtils.UserContext::setLocale, getString(claims, UserContextUtils.UserContext::getLocale))
                .with(UserContextUtils.UserContext::setParentId, getString(claims, UserContextUtils.UserContext::getParentId))
                .with(UserContextUtils.UserContext::setSpanId, getString(claims, UserContextUtils.UserContext::getSpanId))
                .with(UserContextUtils.UserContext::setTraceId, getString(claims, UserContextUtils.UserContext::getTraceId))
                .with(UserContextUtils.UserContext::setTokenSign, getString(claims, UserContextUtils.UserContext::getTokenSign))
                .with(UserContextUtils.UserContext::setVersion, getString(claims, UserContextUtils.UserContext::getVersion))
                .with(UserContextUtils.UserContext::setAppKey, getString(claims, UserContextUtils.UserContext::getAppKey))
                .build();
    }

    public Long getLong(Map<String, Claim> claims, Func1<UserContextUtils.UserContext, Long> function) {
        if (claims == null || function == null) {
            return null;
        }
        Claim claim = claims.get(LambdaUtil.getFieldName(function));
        if (claim == null || claim.isNull()) {
            return null;
        }
        return claim.asLong();
    }

    public Integer getInt(Map<String, Claim> claims, Func1<UserContextUtils.UserContext, Integer> function) {
        if (claims == null || function == null) {
            return null;
        }
        Claim claim = claims.get(LambdaUtil.getFieldName(function));
        if (claim == null || claim.isNull()) {
            return null;
        }
        return claim.asInt();
    }

    public String getString(Map<String, Claim> claims, Func1<UserContextUtils.UserContext, String> function) {
        Claim claim = claims.get(LambdaUtil.getFieldName(function));
        if (null == claim || claim.isNull()) {
            return null;
        }
        return claim.asString();
    }
}