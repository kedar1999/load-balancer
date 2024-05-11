package com.network.loadbalancer.service;

import com.network.loadbalancer.model.AppServerDetail;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Configuration
@Getter
public class HealthCheckVerifier {

    @Autowired
    private RestHttpService restHttpService;

    private boolean isMocked = true;

    @Scheduled(cron = "0 * * * *")
    public void scheduleHealthCheckCron() {
        List<AppServerDetail> appServerDetailList = AppServerManager.getAllAppServers();
        for (AppServerDetail appServerDetail: appServerDetailList) {

            AppServerDetail.YesNoUnknown yesNoUnknown = healthCheck(appServerDetail, isMocked);
            AppServerManager.updateHealthStatus(appServerDetail.getUrl(), yesNoUnknown);
        }
    }

    public AppServerDetail.YesNoUnknown healthCheck(AppServerDetail appServerDetail, boolean isMocked) {
        if (isMocked) {
            return AppServerDetail.YesNoUnknown.YES;
        }
        var response = restHttpService.makeActualHttpCall(appServerDetail.getUrl(), null, appServerDetail.getHealthCheckUrl().substring(appServerDetail.getUrl().length()), null, HttpMethod.GET);
        if (response.getStatusCode().is2xxSuccessful()) {
            appServerDetail.setIsHealthy(AppServerDetail.YesNoUnknown.YES);
            return AppServerDetail.YesNoUnknown.YES;
        } else {
            return AppServerDetail.YesNoUnknown.NO;
        }
    }
}
