package com.network.loadbalancer.service.request.forward.algorithm;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("requestForwardDecider")
@DependsOn("loadBalancerConfig")
public abstract class RequestForwardDecider {

    protected static final Map<Algorithm, RequestForwardDecider> mapAlgoRequestForwardDeciderMap = new HashMap<>();

    public abstract Algorithm getAlgorithm();

    public abstract String getAppServerUrlToForward();

    public abstract void handleAppServerRegistration(String appServerUrl);
    public abstract void handleAppServerRemoval(String appServerUrl);
    public abstract void handleHealthSuccess(String appServerUrl);
    public abstract void handleHealthFailure(String appServerUrl);

    public enum Algorithm {
        ROUND_ROBIN,
        LEAST_CONNECTIONS
    }

    @PostConstruct
    public void constructMap() {
        mapAlgoRequestForwardDeciderMap.put(this.getAlgorithm(), this);
    }

    public static String getAppServerUrl(Algorithm algorithm) {
        return mapAlgoRequestForwardDeciderMap.get(algorithm).getAppServerUrlToForward();
    }

    public static void registerApiServer(String appServer) {
        for (RequestForwardDecider requestForwardDecider: mapAlgoRequestForwardDeciderMap.values()) {
            requestForwardDecider.handleAppServerRegistration(appServer);
        }
    }

    public static void removeApiServer(String appServer) {
        for (RequestForwardDecider requestForwardDecider: mapAlgoRequestForwardDeciderMap.values()) {
            requestForwardDecider.handleAppServerRemoval(appServer);
        }
    }

    public static void healthSuccess(String appServer) {
        for (RequestForwardDecider requestForwardDecider: mapAlgoRequestForwardDeciderMap.values()) {
            requestForwardDecider.handleHealthSuccess(appServer);
        }
    }

    public static void healthFailure(String appServer) {
        for (RequestForwardDecider requestForwardDecider: mapAlgoRequestForwardDeciderMap.values()) {
            requestForwardDecider.handleHealthFailure(appServer);
        }
    }
}
