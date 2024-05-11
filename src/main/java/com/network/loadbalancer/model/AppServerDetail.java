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
    private YesNoUnknown isHealthy;
    private int currentConnections;

    public AppServerDetail(AppServerDetail appServerDetail) {
        this.url = appServerDetail.getUrl();
        this.healthCheckUrl = appServerDetail.getHealthCheckUrl();
        this.isHealthy = appServerDetail.getIsHealthy() != null ? appServerDetail.getIsHealthy() : YesNoUnknown.UNKNOWN;
        this.currentConnections = 0;
    }

    public enum YesNoUnknown {
        YES,
        NO,
        UNKNOWN
    }
}
