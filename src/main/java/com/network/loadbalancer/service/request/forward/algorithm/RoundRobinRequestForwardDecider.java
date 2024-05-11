package com.network.loadbalancer.service.request.forward.algorithm;

import com.network.loadbalancer.config.LoadBalancerApplicationContext;
import com.network.loadbalancer.config.LoadBalancerConfig;
import com.network.loadbalancer.model.AppServerDetail;
import com.network.loadbalancer.service.AppServerManager;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoundRobinRequestForwardDecider extends RequestForwardDecider {

    private static int lastRequestServedBy = 0;
    private static List<String> appServers = new ArrayList<>();

    @Override
    public Algorithm getAlgorithm() {
        return Algorithm.ROUND_ROBIN;
    }

    @Override
    public synchronized String getAppServerUrlToForward() {
        return getAppServer(lastRequestServedBy++);
    }

    @Override
    public void handleAppServerRegistration(String appServerUrl) {
        appServers.add(appServerUrl);
    }

    @Override
    public void handleAppServerRemoval(String appServerUrl) {
        appServers = appServers.stream().filter(it -> !it.equalsIgnoreCase(appServerUrl)).collect(Collectors.toList());
    }

    @Override
    public void handleHealthSuccess(String appServerUrl) {
        if (!appServers.contains(appServerUrl)) {
            appServers.add(appServerUrl);
        }
    }

    @Override
    public void handleHealthFailure(String appServerUrl) {
        appServers.remove(appServerUrl);
    }

    public static String getAppServer(int no) {
        return appServers.get(no % AppServerManager.appServers.size());
    }

    @PostConstruct
    public void construct() {
        LoadBalancerConfig loadBalancerConfig = LoadBalancerApplicationContext.getBean(LoadBalancerConfig.class);
        for (AppServerDetail appServerDetail: loadBalancerConfig.getAppServerDetails()) {
            appServers.add(appServerDetail.getUrl());
        }
    }
}
