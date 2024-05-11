package com.network.loadbalancer.config;

import com.network.loadbalancer.model.AppServerDetail;
import com.network.loadbalancer.service.AppServerManager;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "load-balancer")
@Data
@Configuration("loadBalancerConfig")
public class LoadBalancerConfig {
    private String algorithm;
    private List<AppServerDetail> appServerDetails;
}
