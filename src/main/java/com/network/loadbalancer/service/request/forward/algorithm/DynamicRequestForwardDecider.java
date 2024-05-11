package com.network.loadbalancer.service.request.forward.algorithm;

import jakarta.annotation.PostConstruct;
import org.apache.catalina.connector.Request;

import java.util.HashMap;
import java.util.Map;

public abstract class DynamicRequestForwardDecider extends RequestForwardDecider {

    //private static final Map<Algorithm, DynamicRequestForwardDecider> mapAlgoRequestForwardDeciderMap = new HashMap<>();

    public abstract void handleRequestTerminated(String appServerUrl);
    public abstract void handleRequestInitiated(String appServerUrl);

    @PostConstruct
    public void constructMap() {
        RequestForwardDecider.mapAlgoRequestForwardDeciderMap.put(this.getAlgorithm(), this);
    }

    public static void requestInitiated(String appServer) {
        RequestForwardDecider.mapAlgoRequestForwardDeciderMap.values()
                .forEach(forwardDecider ->
                {
                    try {
                        ((DynamicRequestForwardDecider)forwardDecider).handleRequestInitiated(appServer);
                    } catch (Exception e) {

                    }

                });

    }

    public static void requestTerminated(String appServer) {
        RequestForwardDecider.mapAlgoRequestForwardDeciderMap.values().forEach(forwardDecider ->
        {
            try {
                ((DynamicRequestForwardDecider)forwardDecider).handleRequestTerminated(appServer);
            } catch (Exception e) {

            }
        });
    }
}
