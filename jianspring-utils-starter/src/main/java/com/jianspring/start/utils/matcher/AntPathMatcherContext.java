package com.jianspring.start.utils.matcher;

import org.springframework.util.AntPathMatcher;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author:  InfoInsights
 * @Date: 2023/4/24 上午10:18
 * @Version: 1.0.0
 */

public class AntPathMatcherContext {

    /**
     * 表达式
     */
    private final Set<String> patterns;

    private final AntPathMatcher antPathMatcher;

    public AntPathMatcherContext(Set<String> whiteUrls) {
        this.patterns = whiteUrls;
        this.antPathMatcher = new AntPathMatcher();
    }

    public AntPathMatcherContext(String[] whiteUrls) {
        this.patterns = new HashSet<>();
        Collections.addAll(this.patterns, whiteUrls);
        this.antPathMatcher = new AntPathMatcher();
    }

    public AntPathMatcherContext(List<String> patterns) {
        this.patterns = new HashSet<>(patterns);
        this.antPathMatcher = new AntPathMatcher();
    }

    public boolean matcher(String matcher) {
        if (null == patterns || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (antPathMatcher.match(pattern, matcher)) {
                return true;
            }
        }
        return false;
    }

}