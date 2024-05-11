package com.network.loadbalancer.service;

import com.network.loadbalancer.config.LoadBalancerApplicationContext;
import com.network.loadbalancer.config.LoadBalancerConfig;
import com.network.loadbalancer.model.AppServerDetail;
import com.network.loadbalancer.service.request.forward.algorithm.RequestForwardDecider;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppServerManager {

    public static List<String> appServers = new ArrayList<>();
    private static Map<String, AppServerDetail> appServerDetailMap = new HashMap<>();

    private final LoadBalancerConfig loadBalancerConfig;

    public static List<AppServerDetail> getAllAppServers() {
        return appServerDetailMap.values().stream().map(AppServerDetail::new).toList();
    }

    @Transactional
    public static AppServerDetail registerAppServer(AppServerDetail appServerDetail) throws Exception {
        if (validateAppServerDetail(appServerDetail)) {
            HealthCheckVerifier healthCheckVerifier = LoadBalancerApplicationContext.getBean(HealthCheckVerifier.class);
            if (healthCheckVerifier != null) {
                AppServerDetail.YesNoUnknown yesNoUnknown = healthCheckVerifier.healthCheck(appServerDetail, healthCheckVerifier.isMocked());
                appServerDetail.setIsHealthy(yesNoUnknown);
            }

            if (appServerDetailMap.containsKey(appServerDetail.getUrl())) {
                addAppServerDetails(appServerDetail);
            } else {
                addAppServerDetails(appServerDetail);
                appServers.add(appServerDetail.getUrl());
                RequestForwardDecider.registerApiServer(appServerDetail.getUrl());
            }
        } else {
            throw new Exception("Invalid Request");
        }
        return new AppServerDetail(appServerDetailMap.get(appServerDetail.getUrl()));
    }

    @Transactional
    public static AppServerDetail removeAppServer(AppServerDetail appServerDetail) throws Exception {
        if (appServerDetail != null && !StringUtil.isNullOrEmpty(appServerDetail.getUrl())) {
            if (appServerDetailMap.containsKey(appServerDetail.getUrl())) {
                appServerDetailMap.remove(appServerDetail.getUrl());
                appServers.remove(appServerDetail.getUrl());
                RequestForwardDecider.removeApiServer(appServerDetail.getUrl());
            }
        }
        else {
            throw new Exception("Invalid Request");
        }
        return new AppServerDetail(appServerDetail);
    }

    private static void addAppServerDetails(AppServerDetail newAppServerDetail) {
        AppServerDetail appServerDetail = appServerDetailMap.containsKey(newAppServerDetail.getUrl()) ?
                appServerDetailMap.get(newAppServerDetail.getUrl()) : new AppServerDetail();

        appServerDetail.setUrl(newAppServerDetail.getUrl());
        appServerDetail.setHealthCheckUrl(newAppServerDetail.getHealthCheckUrl());
        appServerDetailMap.put(appServerDetail.getUrl(), appServerDetail);
    }

    private static boolean validateAppServerDetail(AppServerDetail appServerDetail) {
        return appServerDetail != null && !StringUtil.isNullOrEmpty(appServerDetail.getUrl()) && !StringUtil.isNullOrEmpty(appServerDetail.getHealthCheckUrl());
    }

    @PostConstruct
    public void construct() {
        List<AppServerDetail> appServerDetails = this.loadBalancerConfig.getAppServerDetails();
        appServerDetailMap = new HashMap<>();
        for (AppServerDetail appServerDetail: appServerDetails)
            appServerDetailMap.put(appServerDetail.getUrl(), appServerDetail);
        appServers = new ArrayList<>(appServerDetailMap.keySet().stream().toList());
    }

    @Transactional
    public static void updateHealthStatus(String appServer, AppServerDetail.YesNoUnknown yesNoUnknown) {
        AppServerDetail appServerDetail = appServerDetailMap.get(appServer);
        if (!appServerDetail.getIsHealthy().equals(yesNoUnknown)) {
            appServerDetail.setIsHealthy(yesNoUnknown);
            if (yesNoUnknown.equals(AppServerDetail.YesNoUnknown.NO)) {
                if (appServers.contains(appServer)) {
                    appServers = appServers.stream().filter(it -> !it.equalsIgnoreCase(appServer)).collect(Collectors.toList());
                    RequestForwardDecider.healthFailure(appServer);
                }
            } else {
                appServers.add(appServer);
                RequestForwardDecider.healthSuccess(appServer);
            }
        }
    }
}
