package com.network.loadbalancer.service.request.forward.algorithm;

import jakarta.annotation.PostConstruct;
import org.apache.catalina.connector.Request;

import java.util.HashMap;
import java.util.Map;

public abstract class DynamicRequestForwardDecider extends RequestForwardDecider {

    private static final Map<Algorithm, DynamicRequestForwardDecider> mapAlgoRequestForwardDeciderMap = new HashMap<>();

    public abstract void handleRequestTerminated(String appServerUrl);
    public abstract void handleRequestInitiated(String appServerUrl);

    @PostConstruct
    public void constructMap() {
        mapAlgoRequestForwardDeciderMap.put(this.getAlgorithm(), this);
    }

    public static void requestInitiated(String appServer) {
        mapAlgoRequestForwardDeciderMap.values().forEach(forwardDecider -> forwardDecider.handleRequestInitiated(appServer));
    }

    public static void requestTerminated(String appServer) {
        mapAlgoRequestForwardDeciderMap.values().forEach(forwardDecider -> forwardDecider.handleRequestTerminated(appServer));
    }
}
