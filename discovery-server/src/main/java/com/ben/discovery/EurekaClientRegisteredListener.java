package com.ben.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EurekaClientRegisteredListener implements ApplicationListener<EurekaInstanceRegisteredEvent> {

    @Override
    public void onApplicationEvent(EurekaInstanceRegisteredEvent event) {
        log.info("[DISCOVERY-SERVER]: Service name: {} | Service host: {} | Service port: {}",
                event.getInstanceInfo().getAppName(),
                event.getInstanceInfo().getHostName() + "/" + event.getInstanceInfo().getIPAddr(),
                event.getInstanceInfo().getPort());
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }

}
