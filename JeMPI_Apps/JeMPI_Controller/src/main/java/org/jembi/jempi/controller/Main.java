package org.jembi.jempi.controller;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.LinkStatsMeta;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.Properties;

public final class Main {

   private static final Logger LOGGER = LogManager.getLogger(Main.class);
   private static final Deserializer<String> STRING_DESERIALIZER = new StringDeserializer();
   private static final Deserializer<LinkStatsMeta> LINK_STATS_META_DESERIALIZER =
         new JsonPojoDeserializer<>(LinkStatsMeta.class);
   private static final Serde<String> STRING_SERDE = Serdes.String();
   private static final Serde<LinkStatsMeta> LINK_STATS_META_SERDE =
         Serdes.serdeFrom(new JsonPojoSerializer<>(), LINK_STATS_META_DESERIALIZER);

   private Main() {
   }

   public static void main(final String[] args) {
      new Main().run();
   }

   public static Topology createTopology() {
      StoreBuilder<KeyValueStore<String, LinkStatsMeta>> stateStoreBuilder =
            Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(SPLinkStatsMeta.STATE_STORE_NAME),
                                        STRING_SERDE,
                                        LINK_STATS_META_SERDE);

      return new Topology()
            .addSource("Source",
                       STRING_DESERIALIZER, LINK_STATS_META_DESERIALIZER,
                       GlobalConstants.TOPIC_INTERACTION_PROCESSOR_CONTROLLER)
            .addProcessor("Process", SPLinkStatsMeta::new, "Source")
            .addStateStore(stateStoreBuilder, "Process");
   }

   private Properties getProps() {
      final Properties props = new Properties();
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID + "-SP_TALLIES_MU");
      props.put(StreamsConfig.POLL_MS_CONFIG, 50);
      return props;
   }

   public Behavior<Void> create() {
      return Behaviors.setup(context -> {
         final var backEndActor = context.spawn(BackEnd.create(), "BackEnd");

         context.watch(backEndActor);
         final var spAuditTrail = new SPAuditTrail();
         spAuditTrail.open();
         final var spNotification = new SPNotification();
         spNotification.open();
         final var spInteractions = new SPInteractions();
         spInteractions.open(context.getSystem(), backEndActor);
         final var spMU = new SPMU();
         spMU.open(context.getSystem(), backEndActor);
//         new SPInteractionProcessor().open();
         final var streaming = new KafkaStreams(createTopology(), getProps());
         streaming.start();
         final var httpServer = new HttpServer();
         httpServer.open(context.getSystem(), backEndActor);
         return Behaviors.receive(Void.class).onSignal(Terminated.class, sig -> {
            httpServer.close(context.getSystem());
            streaming.close();
            return Behaviors.stopped();
         }).build();
      });
   }

   private void run() {
      LOGGER.info("CONFIG: {} {} {} {} {}",
                  AppConfig.POSTGRESQL_NOTIFICATIONS_DB,
                  AppConfig.POSTGRESQL_AUDIT_DB,
                  AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                  AppConfig.KAFKA_APPLICATION_ID,
                  AppConfig.KAFKA_CLIENT_ID);

      ActorSystem.create(this.create(), "ControllerApp");

   }
}
