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

import java.util.Date;
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

    /**
     * 生成JWT令牌
     *
     * @param userContext 用户上下文
     * @return JWT令牌
     */
    public String generateToken(UserContextUtils.UserContext userContext) {
        if (userContext == null || StrUtil.isEmpty(iamJwtProperties.getSign())) {
            return null;
        }

        Date now = new Date();
        Date expiry = new Date(now.getTime() + iamJwtProperties.getExpiration());

        return JWT.create()
                .withIssuer("jian-spring")
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .withClaim(LambdaUtil.getFieldName(UserContextUtils.UserContext::getAccountId), userContext.getAccountId())
                .withClaim(LambdaUtil.getFieldName(UserContextUtils.UserContext::getUserId), userContext.getUserId())
                .withClaim(LambdaUtil.getFieldName(UserContextUtils.UserContext::getUserType), userContext.getUserType())
                .withClaim(LambdaUtil.getFieldName(UserContextUtils.UserContext::getMobileNumber), userContext.getMobileNumber())
                .withClaim(LambdaUtil.getFieldName(UserContextUtils.UserContext::getName), userContext.getName())
                .withClaim(LambdaUtil.getFieldName(UserContextUtils.UserContext::getUserName), userContext.getUserName())
                .withClaim(LambdaUtil.getFieldName(UserContextUtils.UserContext::getTenantId), userContext.getTenantId())
                .withClaim(LambdaUtil.getFieldName(UserContextUtils.UserContext::getLocale), userContext.getLocale())
                .withClaim(LambdaUtil.getFieldName(UserContextUtils.UserContext::getTokenSign), userContext.getTokenSign())
                .withClaim(LambdaUtil.getFieldName(UserContextUtils.UserContext::getVersion), userContext.getVersion())
                .withClaim(LambdaUtil.getFieldName(UserContextUtils.UserContext::getAppKey), userContext.getAppKey())
                .sign(Algorithm.HMAC256(iamJwtProperties.getSign()));
    }

    /**
     * 验证令牌是否有效
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        if (StrUtil.isEmpty(token) || StrUtil.isEmpty(iamJwtProperties.getSign())) {
            return false;
        }
        try {
            JWT.require(Algorithm.HMAC256(iamJwtProperties.getSign()))
                    .withIssuer("jian-spring")
                    .build()
                    .verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
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