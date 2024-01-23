package org.jembi.jempi.shared.libs.interactionProcessor;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.libs.interactionProcessor.models.InteractionProcessorEnvelop;
import org.jembi.jempi.shared.libs.interactionProcessor.models.OnNewInteractionInteractionProcessorEnvelope;
import org.jembi.jempi.shared.libs.interactionProcessor.models.OnProcessCandidatesInteractionProcessorEnvelope;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.UUID;


public final class InteractionProcessorConnector {

    private static final Logger LOGGER = LogManager.getLogger(InteractionProcessorConnector.class);
    private MyKafkaProducer<String, InteractionProcessorEnvelop> kafkaProducer;
    public InteractionProcessorConnector(final String bootstrapperServer) {
        kafkaProducer = new MyKafkaProducer<>(bootstrapperServer,
                                GlobalConstants.TOPIC_INTERACTION_PROCESSOR_CONTROLLER,
                                new StringSerializer(), new JsonPojoSerializer<>(),
                "INTERACTION_PROCESSOR_CONNECTOR" + UUID.randomUUID());
    }

    private void produceMessage(final InteractionProcessorEnvelop interactionProcessorEnvelop) {
        kafkaProducer.produceAsync(UUID.randomUUID().toString(),
                                    interactionProcessorEnvelop,
                                    ((metadata, exception) -> {
                                        if (exception != null) {
                                            LOGGER.error(exception.toString());
                                        }
                                    }));

    }
    public void sendOnNewNotification(final Interaction interaction, final String envelopeStan) {
        produceMessage(new InteractionProcessorEnvelop(InteractionProcessorEvents.ON_NEW_INTERACTION,
                new OnNewInteractionInteractionProcessorEnvelope(interaction, envelopeStan)));
    }
    public void sendOnProcessCandidates(final Interaction interaction, final String envelopeStan, final Float matchThreshold) {
        produceMessage(new InteractionProcessorEnvelop(InteractionProcessorEvents.ON_PROCESS_CANDIDATES,
                new OnProcessCandidatesInteractionProcessorEnvelope(interaction, envelopeStan, matchThreshold)));
    }
}
