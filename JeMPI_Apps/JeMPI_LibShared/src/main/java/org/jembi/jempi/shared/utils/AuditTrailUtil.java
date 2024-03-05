package org.jembi.jempi.shared.utils;

import java.sql.Timestamp;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.AuditEvent;
import org.jembi.jempi.shared.models.AuditEventType;
import org.jembi.jempi.shared.models.ExpandedAuditEvent;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;


public final class AuditTrailUtil {
    private MyKafkaProducer<String, ExpandedAuditEvent> topicAuditEvents = null;
    private static final Logger LOGGER = LogManager.getLogger(AuditTrailUtil.class);

    public AuditTrailUtil(final MyKafkaProducer<String, ExpandedAuditEvent> topicAuditEvents) {
        this.topicAuditEvents = topicAuditEvents;
    }

    public <T> void sendAuditEvent(
            final String interactionID,
            final String goldenID,
            final String event,
            final T eventData,
            final AuditEventType eventType) {

        var serializedEventData = getSerializedEventData(eventData);
        var auditEvent = new AuditEvent(
                new Timestamp(System.currentTimeMillis()),
                null,
                interactionID,
                goldenID,
                event
        );

        topicAuditEvents.produceAsync(goldenID,
             new ExpandedAuditEvent(auditEvent, eventType, serializedEventData),
                (metadata, exception) -> {
                    if (exception != null) {
                        LOGGER.error(exception.getMessage(), exception);
                    }
                });
    }

    private <T> String getSerializedEventData(final T eventData) {
        try {
            return eventData != null ? OBJECT_MAPPER.writeValueAsString(eventData) : null;
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize event data", e);
            return null;
        }
    }
}
