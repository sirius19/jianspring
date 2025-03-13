package com.jianspring.starter.cloud.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author:  InfoInsights
 * @Date: 2023/2/21 下午1:41
 * @Version: 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public String health() {
        return "success";
    }

}