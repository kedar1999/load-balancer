package com.network.loadbalancer.service.request.forward.algorithm;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public abstract class RequestForwardDecider {

    private static final Map<Algorithm, RequestForwardDecider> mapAlgoRequestForwardDeciderMap = new HashMap<>();

    public abstract Algorithm getAlgorithm();

    public abstract String getAppServerUrlToForward();

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
}
