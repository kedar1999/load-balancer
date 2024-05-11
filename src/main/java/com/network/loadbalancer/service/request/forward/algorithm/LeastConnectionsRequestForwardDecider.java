package com.network.loadbalancer.service.request.forward.algorithm;

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
}
