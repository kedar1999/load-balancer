package com.network.loadbalancer.service.request.forward.algorithm;

import com.network.loadbalancer.service.AppServerManager;
import org.springframework.stereotype.Service;

@Service
public class RoundRobinRequestForwardDecider extends RequestForwardDecider {

    private static int lastRequestServedBy = 0;

    @Override
    public Algorithm getAlgorithm() {
        return Algorithm.ROUND_ROBIN;
    }

    @Override
    public synchronized String getAppServerUrlToForward() {
        return AppServerManager.getAppServer(lastRequestServedBy++);
    }
}
