package com.jianspring.starter.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.jianspring.starter.commons.error.CommonErrorCode;
import com.jianspring.starter.commons.exception.SentinelBlockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class SentinelBlockExceptionHandler implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        if (e instanceof FlowException) {
            throw SentinelBlockException.of(CommonErrorCode.ERROR.getCode(), "请求过于频繁！");
        } else if (e instanceof DegradeException) {
            throw SentinelBlockException.of(CommonErrorCode.ERROR.getCode(), "服务不稳定，请稍后重试！");
        } else if (e instanceof ParamFlowException) {
            throw SentinelBlockException.of(CommonErrorCode.ERROR.getCode(), "热点参数限流");
        } else if (e instanceof SystemBlockException) {
            throw SentinelBlockException.of(CommonErrorCode.ERROR.getCode(), "触发系统保护规则");
        } else if (e instanceof AuthorityException) {
            throw SentinelBlockException.of(CommonErrorCode.ERROR.getCode(), "授权规则不通过");
        } else {
            throw SentinelBlockException.of(CommonErrorCode.ERROR);
        }
    }
}
