package com.unmsm.marketplace.postventa_service.client;

import com.unmsm.marketplace.postventa_service.dto.AnalyticsEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "analytics-service", url = "${analytics.service.url}")
public interface AnalyticsClient {

    @PostMapping("/api/events")
    void sendEvent(@RequestHeader("x-api-key") String apiKey, @RequestBody AnalyticsEvent event);
}
