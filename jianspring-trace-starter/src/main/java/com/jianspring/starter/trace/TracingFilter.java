package com.jianspring.starter.trace;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.jianspring.start.utils.id.ObjectId;
import com.jianspring.starter.commons.UserContextUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class TracingFilter extends OncePerRequestFilter implements Ordered {
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String trace = request.getHeader("JIAN-TRACE-ID");
            String parent;
            String span;
            if (!StringUtils.hasText(trace)) {
                trace = ObjectId.nextId();
                span = ObjectId.nextId();
                parent = span;
                UserContextUtils.setOnlyTracing(span, span, trace);
            } else {
                parent = request.getHeader("JIAN-SPAN-ID");
                span = ObjectId.nextId();
                UserContextUtils.setOnlyTracing(parent, span, trace);
            }
            MDC.put("traceId", trace);
            MDC.put("parentId", parent);
            MDC.put("spanId", span);
            MDC.put("ip", JakartaServletUtil.getClientIP(request));
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
            UserContextUtils.clear();
        }
    }
}
