package com.network.loadbalancer.config;

import com.network.loadbalancer.model.AppServerDetail;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "load-balancer")
@Data
public class LoadBalancerConfig {
    private String algorithm;
    private List<AppServerDetail> appServerDetails;
}
