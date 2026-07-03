package com.unmsm.marketplace.postventa_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AnalyticsEvent(
    @JsonProperty("event_id") String eventId,
    String type,
    String service,
    @JsonProperty("aggregate_type") String aggregateType,
    @JsonProperty("aggregate_id") String aggregateId,
    @JsonProperty("vendor_ids") List<String> vendorIds,
    @JsonProperty("event_timestamp") String eventTimestamp,
    Object payload
) {}
