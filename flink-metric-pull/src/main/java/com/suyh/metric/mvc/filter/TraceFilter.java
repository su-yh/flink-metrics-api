package com.suyh.metric.mvc.filter;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@WebFilter("/**")
public class TraceFilter implements Filter {
    public static final String TRACE_ID = "trace-id";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        Long traceId = IdWorker.getId();
        // 生成或获取 traceId，这里使用 UUID 作为示例
        // 将 traceId 放入 MDC
        MDC.put(TRACE_ID, traceId + "");
        response.setHeader(TRACE_ID, traceId + "");
        request.setAttribute(TRACE_ID, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }
}
