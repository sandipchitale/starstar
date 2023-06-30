package com.example.starstar;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.core.ApplicationFilterChain;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.debug.DebugFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Configuration
public class DumpFiltersConfig {
    public static class DumpFilters extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            if (filterChain instanceof ApplicationFilterChain) {
                System.out.println();
                System.out.println("Begin Filters ============================");
                System.out.println("URL: " + request.getMethod() + " " + request.getRequestURI());
//                System.out.println("Headers:");
//                request.getHeaderNames().asIterator().forEachRemaining((String headerName) -> {
//                    System.out.println("\t" + headerName + ": " + request.getHeader(headerName));
//                });
//                System.out.println("Params:");
//                System.out.println(String.valueOf(request.getParameterMap()));
                ApplicationFilterChain applicationFilterChain = (ApplicationFilterChain) filterChain;
                try {
                    Field filters = applicationFilterChain.getClass().getDeclaredField("filters");
                    filters.setAccessible(true);
                    ApplicationFilterConfig[] filterConfigs = (ApplicationFilterConfig[]) filters
                            .get(applicationFilterChain);
                    boolean firstMatched = false;
                    for (ApplicationFilterConfig applicationFilterConfig : filterConfigs) {
                        if (applicationFilterConfig != null) {
                            System.out.println("Filter Name: " + applicationFilterConfig.getFilterName()
                                    + " FilterClass: " + applicationFilterConfig.getFilterClass());
                            if (applicationFilterConfig.getFilterName().equals("springSecurityFilterChain")) {
                                try {
                                    Method getFilter = applicationFilterConfig.getClass()
                                            .getDeclaredMethod("getFilter");
                                    getFilter.setAccessible(true);
                                    DelegatingFilterProxy delegatingFilterProxy = (DelegatingFilterProxy) getFilter
                                            .invoke(applicationFilterConfig);
                                    Field delegateField = DelegatingFilterProxy.class.getDeclaredField("delegate");
                                    delegateField.setAccessible(true);
                                    FilterChainProxy filterChainProxy = null;
                                    if (delegateField.get(delegatingFilterProxy) instanceof FilterChainProxy) {
                                        filterChainProxy = (FilterChainProxy) delegateField.get(delegatingFilterProxy);
                                    }
                                    if (delegateField.get(delegatingFilterProxy) instanceof DebugFilter debugFilter) {
                                        // DebugFilter debugFilter = (DebugFilter) delegateField.get(delegatingFilterProxy);
                                        System.out.println("\torg.springframework.security.web.debug.DebugFilter");
                                        filterChainProxy = debugFilter.getFilterChainProxy();
                                    }
                                    if (filterChainProxy != null) {
                                        List<SecurityFilterChain> filterChains = filterChainProxy.getFilterChains();
                                        System.out.println("Begin Filter Chains ============================");
                                        for (SecurityFilterChain securityFilterChain : filterChains) {
                                            DefaultSecurityFilterChain defaultSecurityFilterChain = (DefaultSecurityFilterChain) securityFilterChain;
                                            RequestMatcher requestMatcher = defaultSecurityFilterChain.getRequestMatcher();
                                            System.out.println("\t" + requestMatcher);
                                            if (requestMatcher instanceof OrRequestMatcher orRequestMatcher) {
                                                // OrRequestMatcher orRequestMatcher = (OrRequestMatcher) requestMatcher;
                                                Field requestMatchersField = ReflectionUtils.findField(OrRequestMatcher.class, "requestMatchers");
                                                ReflectionUtils.makeAccessible(requestMatchersField);
                                                List<RequestMatcher> requestMatchers =
                                                        (List<RequestMatcher>) ReflectionUtils.getField(requestMatchersField, requestMatcher);
                                                requestMatchers.forEach((RequestMatcher rm) -> {
                                                    System.out.println("\t\t" + rm);
                                                });
                                            }
                                            if (requestMatcher instanceof AndRequestMatcher andRequestMatcher) {
                                                // AndRequestMatcher andRequestMatcher = (AndRequestMatcher) requestMatcher;
                                                Field requestMatchersField = ReflectionUtils.findField(OrRequestMatcher.class, "requestMatchers");
                                                ReflectionUtils.makeAccessible(requestMatchersField);
                                                List<RequestMatcher> requestMatchers =
                                                        (List<RequestMatcher>) ReflectionUtils.getField(requestMatchersField, requestMatcher);
                                                requestMatchers.forEach((RequestMatcher rm) -> {
                                                    System.out.println("\t\t" + rm);
                                                });
                                            }
                                            if (requestMatcher instanceof NegatedRequestMatcher negatedRequestMatcher) {
                                                // NegatedRequestMatcher negatedRequestMatcher = (NegatedRequestMatcher) requestMatcher;
                                                Field requestMatcherField = ReflectionUtils.findField(OrRequestMatcher.class, "requestMatcher");
                                                ReflectionUtils.makeAccessible(requestMatcherField);
                                                RequestMatcher rm =
                                                        (RequestMatcher) ReflectionUtils.getField(requestMatcherField, requestMatcher);
                                                System.out.println("\t\t" + rm);
                                            }
                                            if (!firstMatched && defaultSecurityFilterChain.getRequestMatcher().matches(request)) {
                                                firstMatched = true;
                                                System.out.println("\t\t" + request.getMethod() + " " + request.getRequestURI() + " Matched");
                                            }
                                            List<Filter> securityFilters = securityFilterChain.getFilters();
                                            for (Filter securityFilter : securityFilters) {
                                                System.out.println("\t\t" + securityFilter);
                                            }
                                        }
                                        System.out.println("End Filter Chains ==============================");
                                    }
                                } catch (NoSuchMethodException | InvocationTargetException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                    }
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                         | IllegalAccessException e) {
                    System.out.println(e.getMessage());
                }
            }
            System.out.println("End Filters ==============================");
            filterChain.doFilter(request, response);
        }
    }

    @Bean
    FilterRegistrationBean<DumpFilters> filters() {
        FilterRegistrationBean<DumpFilters> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new DumpFilters());
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}