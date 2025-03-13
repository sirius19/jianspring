package com.jianspring.starter.trace;

import cn.hutool.core.codec.Base64;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.jianspring.start.utils.lambda.GenericEntityBuilder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author: InfoInsights
 * @Date: 2023/2/22 下午5:06
 * @Version: 1.0.0
 */
@Slf4j
public class TracingLogFilter extends OncePerRequestFilter implements Ordered {
    private static final String FILE_BODY = "file body";

    private static final String LONG_BODY = "long body";

    private final TracingLogProperties tracingLogProperties;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher(); // 复用路径匹配器
    private static final Set<String> DECODE_HEADERS = Set.of("JIAN-NAME", "JIAN-USER-NAME"); // 需要解码的请求头
    private static final Set<MediaType> FILE_MEDIA_TYPES = Set.of( // 文件类型集合
            MediaType.APPLICATION_OCTET_STREAM,
            MediaType.IMAGE_GIF,
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_PNG,
            MediaType.APPLICATION_PDF
    );

    public TracingLogFilter(final TracingLogProperties tracingLogProperties) {
        this.tracingLogProperties = tracingLogProperties;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            GenericEntityBuilder<Tracing> builder = GenericEntityBuilder.of(Tracing::new);
            builder.with(Tracing::setIp, MDC.get("ip"));
            builder.with(Tracing::setPath, request.getRequestURI());
            builder.with(Tracing::setMethod, request.getMethod());
            builder.with(Tracing::setHeader, getHeaders(request));
            builder.with(Tracing::setQueryParams, request.getQueryString());
            builder.with(Tracing::setRequestBody, getRequestBody(request));
            builder.with(Tracing::setResponseBody, getResponseBody(response));
            builder.with(Tracing::setCostTime, System.currentTimeMillis() - start);
            builder.with(Tracing::setStatus, response.getStatus());
            log.info("-----------------------Tracing Start---------------------------------");
            log.info("Tracing : {}", builder.build());
            log.info("-----------------------Tracing End-----------------------------------");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludedEndpoints = tracingLogProperties.getExcludedEndpoints();
        if (excludedEndpoints == null || excludedEndpoints.length == 0) {
            return false;
        }
        return Arrays.stream(excludedEndpoints)
                .anyMatch(e -> PATH_MATCHER.match(e, request.getServletPath()));

    }

    private String getHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headerMap = new HashMap<>();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            if (DECODE_HEADERS.contains(headerName.toUpperCase())) {
                headerMap.put(headerName,
                        StringUtils.hasText(headerValue) ? Base64.decodeStr(headerValue) : headerValue);
            } else {
                headerMap.put(headerName, headerValue);
            }
        }
        return headerMap.toString();
    }

    private String getRequestBody(HttpServletRequest request) {
        if (JakartaServletUtil.isMultipart(request)) {
            return FILE_BODY;
        }
        if (isFileContentType(request.getContentType())) {
            return FILE_BODY;
        }
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (null == wrapper) {
            return null;
        }
        try {
            byte[] byteArray = wrapper.getContentAsByteArray();
            if (byteArray.length == 0) return null; // 明确空内容处理

            String charset = request.getCharacterEncoding() != null ?
                    request.getCharacterEncoding() : StandardCharsets.UTF_8.name();
            String body = new String(byteArray, charset);
            return body.length() > 20480 ?
                    body.substring(0, 20480) + "...[TRUNCATED]" : body;
        } catch (Exception e) {
            log.warn("", e);
            return null;
        }
    }

    private String getResponseBody(HttpServletResponse response) {
        if (isFileContentType(response.getContentType())) {
            return FILE_BODY;
        }
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (null == wrapper) {
            return null;
        }
        try {
            byte[] byteArray = wrapper.getContentAsByteArray();
            if (byteArray.length == 0) return null;

            String charset = response.getCharacterEncoding() != null ?
                    response.getCharacterEncoding() : StandardCharsets.UTF_8.name();
            String body = new String(byteArray, charset);

            wrapper.copyBodyToResponse(); // 确保始终执行
            return body.length() > 20480 ?
                    body.substring(0, 20480) + "...[TRUNCATED]" : body;
        } catch (IOException ex) {
            log.warn("读取响应体失败: {}", ex.getMessage());
            return null;
        }
    }

    private boolean isFileContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) return false;

        try {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            return FILE_MEDIA_TYPES.contains(mediaType);
        } catch (InvalidMediaTypeException ex) {
            log.debug("无法解析的媒体类型: {}", contentType);
            return false;
        }
    }


}
