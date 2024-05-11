package com.network.loadbalancer.service.request.forward.algorithm;

import com.network.loadbalancer.config.LoadBalancerApplicationContext;
import com.network.loadbalancer.config.LoadBalancerConfig;
import com.network.loadbalancer.model.AppServerDetail;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LeastConnectionsRequestForwardDecider extends DynamicRequestForwardDecider {

    private static final Map<Integer, Set<String>> connectionsToAppServersMap = new HashMap<>();
    private static final Map<String, Integer> appServerToConnectioMap = new TreeMap<>();

    @Override
    public Algorithm getAlgorithm() {
        return Algorithm.LEAST_CONNECTIONS;
    }

    @Override
    public synchronized String getAppServerUrlToForward() {
        return connectionsToAppServersMap.values().stream().findFirst().get().stream().findFirst().get();
    }

    @Override
    public void handleRequestTerminated(String appServerUrl) {
        int currentConnections = appServerToConnectioMap.get(appServerUrl);
        connectionsToAppServersMap.get(currentConnections).remove(appServerUrl);
        int decreasedConnections = currentConnections - 1;
        appServerToConnectioMap.put(appServerUrl, decreasedConnections);

        if (connectionsToAppServersMap.containsKey(decreasedConnections)) {
            Set<String> currentAppServers = connectionsToAppServersMap.get(decreasedConnections);
            currentAppServers.add(appServerUrl);
            connectionsToAppServersMap.put(decreasedConnections, currentAppServers);
        } else {
            Set<String> newAppServers = new HashSet<>();
            newAppServers.add(appServerUrl);
            connectionsToAppServersMap.put(decreasedConnections, newAppServers);
        }
    }

    @Override
    public void handleRequestInitiated(String appServerUrl) {
        int currentConnections;
        int increasedConnections;
        if (appServerToConnectioMap.containsKey(appServerUrl)) {
            currentConnections = appServerToConnectioMap.get(appServerUrl);
            connectionsToAppServersMap.get(currentConnections).remove(appServerUrl);
            increasedConnections =currentConnections + 1;
            appServerToConnectioMap.put(appServerUrl, increasedConnections);
        }
        else {
            increasedConnections = 1;
            appServerToConnectioMap.put(appServerUrl, increasedConnections);
        }

        if (connectionsToAppServersMap.containsKey(increasedConnections)) {
            Set<String> currentAppServers = connectionsToAppServersMap.get(increasedConnections);
            currentAppServers.add(appServerUrl);
            connectionsToAppServersMap.put(increasedConnections, currentAppServers);
        } else {
            Set<String> newAppServers = new HashSet<>();
            newAppServers.add(appServerUrl);
            connectionsToAppServersMap.put(increasedConnections, newAppServers);
        }
    }

    @Override
    public void handleAppServerRegistration(String appServerUrl) {
        appServerToConnectioMap.put(appServerUrl, 0);
        if (connectionsToAppServersMap.containsKey(0)) {
            connectionsToAppServersMap.get(0).add(appServerUrl);
        } else {
            connectionsToAppServersMap.put(0, new HashSet<>());
        }
    }

    @Override
    public void handleAppServerRemoval(String appServerUrl) {
        connectionsToAppServersMap.get(appServerToConnectioMap.get(appServerUrl)).remove(appServerUrl);
        appServerToConnectioMap.remove(appServerUrl);
    }

    @Override
    public void handleHealthSuccess(String appServerUrl) {
        handleAppServerRegistration(appServerUrl);
    }

    @Override
    public void handleHealthFailure(String appServerUrl) {
        handleAppServerRemoval(appServerUrl);
    }

    @PostConstruct
    public void construct() {
        LoadBalancerConfig loadBalancerConfig = LoadBalancerApplicationContext.getBean(LoadBalancerConfig.class);
        for (AppServerDetail appServerDetail: loadBalancerConfig.getAppServerDetails()) {
            appServerToConnectioMap.put(appServerDetail.getUrl(), 0);
            if (connectionsToAppServersMap.containsKey(0)) {
                connectionsToAppServersMap.get(0).add(appServerDetail.getUrl());
            } else {
                Set<String> s = new HashSet<>();
                s.add(appServerDetail.getUrl());
                connectionsToAppServersMap.put(0, s);
            }
        }

    }
}
