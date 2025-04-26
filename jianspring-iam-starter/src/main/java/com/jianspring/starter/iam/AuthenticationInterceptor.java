package com.jianspring.starter.iam;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import com.jianspring.start.utils.lambda.GenericEntityBuilder;
import com.jianspring.starter.commons.UserContextUtils;
import com.jianspring.starter.commons.enums.HeaderEnums;
import com.jianspring.starter.commons.error.CommonErrorCode;
import com.jianspring.starter.commons.exception.BizException;
import com.jianspring.starter.commons.exception.LoginException;
import com.jianspring.starter.feign.enums.JianSpringModel;
import com.jianspring.starter.iam.annotation.IgnoreToken;
import com.jianspring.starter.iam.annotation.RequiresPermissions;
import com.jianspring.starter.iam.service.PermissionChecker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * @author: InfoInsights
 * @Date: 2023/2/20 上午9:42
 * @Version: 1.0.0
 */
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor, Ordered {

    private IamJwtService iamJwtService;
    private IamJwtProperties iamJwtProperties;
    private final PermissionChecker permissionChecker;

    // 构造函数注入依赖
    public AuthenticationInterceptor(IamJwtService iamJwtService,
                                     IamJwtProperties iamJwtProperties,
                                     PermissionChecker permissionChecker) {
        this.iamJwtService = iamJwtService;
        this.iamJwtProperties = iamJwtProperties;
        this.permissionChecker = permissionChecker;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, @NotNull Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            return true;
        }

        // 静态资源放行
        if (request.getRequestURI().matches(".*(css|js|html|png|jpg|jpeg|gif|ico)$")) {
            return true;
        }

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            if (log.isDebugEnabled()) {
                log.debug("handler not instanceof HandlerMethod request uri:{}", request.getRequestURI());
            }
            return true;
        }

        boolean isLoggedIn = handlerUserContext(request);

        // 1. 检查是否需要忽略token验证
        if (handlerMethod.getMethod().isAnnotationPresent(IgnoreToken.class)) {
            if (log.isDebugEnabled()) {
                log.debug("ignore token request uri:{}", request.getRequestURI());
            }
            return true;
        }

        // 2. 检查是否需要权限验证
        RequiresPermissions requiresPermissions = handlerMethod.getMethodAnnotation(RequiresPermissions.class);

        // 需要权限验证时，必须先登录
        if (requiresPermissions != null && !isLoggedIn) {
            log.error("need login for permission check:{}", request.getRequestURI());
            throw LoginException.of(CommonErrorCode.NOT_LOGIN_IN);
        }
        if (!isLoggedIn) {
            String model = request.getHeader("JIAN-MODEL");
            // 处理内部调用
            if (JianSpringModel.INNER_FEIGN.name().equals(model)) {
                return true;
            }

            // 处理网关调用限制
            if (Boolean.FALSE.equals(iamJwtProperties.getAllowDirectInvoke())) {
                if (!JianSpringModel.JIAN_GATEWAY.name().equals(model)) {
                    throw LoginException.of(CommonErrorCode.ILLEGAL_REQUEST);
                }
            }

            log.error("not login:{}", request.getRequestURI());
            throw LoginException.of(CommonErrorCode.NOT_LOGIN_IN);
        }

        // 4. 已登录且需要权限验证，进行权限校验
        if (requiresPermissions != null) {
            boolean hasPermission = permissionChecker.hasPermission(
                    requiresPermissions.value(),
                    requiresPermissions.logical()
            );
            if (!hasPermission) {
                throw BizException.of(CommonErrorCode.HAVE_ON_AUTHORITY);
            }
        }

        return true;
    }

    boolean handlerUserContext(HttpServletRequest request) {
        UserContextUtils.UserContext tokenUserContext = iamJwtService.getLoginBaseInfo(request.getHeader(HeaderEnums.TOKEN.getKey()));
        if (null != tokenUserContext) {
            if (null != request.getHeader(HeaderEnums.TENANT_ID.getKey()) && !request.getHeader(HeaderEnums.TENANT_ID.getKey()).equalsIgnoreCase(tokenUserContext.getTenantId().toString())) {
                throw BizException.of(CommonErrorCode.ILLEGAL_REQUEST);
            }
            if (null != request.getHeader(HeaderEnums.APP_KEY.getKey()) && !request.getHeader(HeaderEnums.APP_KEY.getKey()).equalsIgnoreCase(tokenUserContext.getAppKey())) {
                throw BizException.of(CommonErrorCode.ILLEGAL_REQUEST);
            }
            tokenUserContext.setHint(request.getHeader(HeaderEnums.HINT_KEY.getKey()));
            UserContextUtils.setOnlyUser(tokenUserContext);
            return true;
        }
        UserContextUtils.UserContext headerUserContext = tokenToUserContext(request);
        UserContextUtils.setOnlyUser(headerUserContext);
        return null != headerUserContext.getUserId() && null != headerUserContext.getUserType();
    }

    public UserContextUtils.UserContext tokenToUserContext(HttpServletRequest request) {
        GenericEntityBuilder<UserContextUtils.UserContext> entityBuilder = GenericEntityBuilder.of(UserContextUtils.UserContext::new);
        String userId = request.getHeader(HeaderEnums.USER_ID.getKey());
        if (null != userId) {
            entityBuilder.with(UserContextUtils.UserContext::setUserId, Long.valueOf(userId));
        } else {
            entityBuilder.with(UserContextUtils.UserContext::setUserId, 0L);
        }
        String userType = request.getHeader(HeaderEnums.USER_TYPE.getKey());
        if (null != userType) {
            entityBuilder.with(UserContextUtils.UserContext::setUserType, Integer.valueOf(userType));
        }
        if (null != request.getHeader(HeaderEnums.ACCOUNT_ID.getKey())) {
            entityBuilder.with(UserContextUtils.UserContext::setAccountId, Long.valueOf(request.getHeader(HeaderEnums.ACCOUNT_ID.getKey())));
        }
        if (null != request.getHeader(HeaderEnums.TENANT_ID.getKey())) {
            entityBuilder.with(UserContextUtils.UserContext::setTenantId, Long.valueOf(request.getHeader(HeaderEnums.TENANT_ID.getKey())));
        }
        if (StrUtil.isNotBlank(request.getHeader(HeaderEnums.HINT_KEY.getKey()))) {
            entityBuilder.with(UserContextUtils.UserContext::setHint, request.getHeader(HeaderEnums.HINT_KEY.getKey()));
        }

        String name = request.getHeader(HeaderEnums.NAME.getKey());
        String userName = request.getHeader(HeaderEnums.USER_NAME.getKey());

        return entityBuilder
                .with(UserContextUtils.UserContext::setMobileNumber, request.getHeader(HeaderEnums.USER_MOBILE_NUMBER.getKey()))
                .with(UserContextUtils.UserContext::setName, name != null ? URLDecoder.decode(name, StandardCharsets.UTF_8) : null)
                .with(UserContextUtils.UserContext::setUserName, userName != null ? URLDecoder.decode(userName, StandardCharsets.UTF_8) : null)
                .with(UserContextUtils.UserContext::setLocale, StrUtil.isBlank(request.getHeader(HeaderEnums.LOCALE.getKey())) ? Locale.CHINA.toString() : request.getHeader(HeaderEnums.LOCALE.getKey()))
                .with(UserContextUtils.UserContext::setParentId, request.getHeader(HeaderEnums.PARENT_ID.getKey()))
                .with(UserContextUtils.UserContext::setTraceId, request.getHeader(HeaderEnums.TRACE_ID.getKey()))
                .with(UserContextUtils.UserContext::setSpanId, request.getHeader(HeaderEnums.SPAN_ID.getKey()))
                .with(UserContextUtils.UserContext::setTokenSign, request.getHeader(HeaderEnums.TOKEN_SIGN.getKey()))
                .with(UserContextUtils.UserContext::setVersion, request.getHeader(HeaderEnums.VERSION.getKey()))
                .with(UserContextUtils.UserContext::setAppKey, request.getHeader(HeaderEnums.APP_KEY.getKey()))
                .build();
    }
}