package com.network.loadbalancer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
public class AppServerDetail {
    private String url;
    @Value("health-check-url")
    private String healthCheckUrl;

    public AppServerDetail(AppServerDetail appServerDetail) {
        this.url = appServerDetail.getUrl();
        this.healthCheckUrl = appServerDetail.getHealthCheckUrl();
    }
}
