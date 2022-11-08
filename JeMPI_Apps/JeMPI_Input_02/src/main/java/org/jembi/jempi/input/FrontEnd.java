package org.jembi.jempi.input;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r4.model.Patient;
import org.jembi.jempi.AppConfig;

import java.util.concurrent.CompletionStage;


public class FrontEnd extends AllDirectives {
   private static final Logger LOGGER = LogManager.getLogger(FrontEnd.class);
   final FhirContext ctx = FhirContext.forR4();
   final IParser parser = ctx.newJsonParser();
   private CompletionStage<ServerBinding> binding = null;
   private Http http = null;

   void close(ActorSystem<Void> system) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> system.terminate()); // and shutdown when done
   }

   void open(final ActorSystem<Void> system,
             final ActorRef<BackEnd.Event> backEnd) {
      http = Http.get(system);
      binding = http.newServerAt(AppConfig.HTTP_SERVER_HOST,
                                 AppConfig.HTTP_SERVER_PORT)
                    .bind(this.createRoute(system, backEnd));
      LOGGER.info("Server online at http://{}:{}", AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT);
   }

   private Route postPatient(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return entity(Unmarshaller.entityToString(),
                    json -> {
                       final Patient patient = parser.parseResource(Patient.class, json);
                       LOGGER.debug("{}", patient);

                       final var serialized = parser.setPrettyPrint(false).encodeResourceToString(patient);
                       LOGGER.debug("{}", serialized);

                       final var pat2 = parser.setPrettyPrint(true).encodeResourceToString(patient);
                       LOGGER.debug("{}", pat2);

                       return complete(StatusCodes.NO_CONTENT);
                    });
   }

   private Route createRoute(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return pathPrefix("fhir",
                        () -> concat(
                              post(() -> concat(
                                    path("Patient", () -> postPatient(actorSystem, backEnd)),
                                    path("test2", () -> complete(StatusCodes.OK)))),
                              get(() -> path("Patient", () -> complete(StatusCodes.OK)))));
   }

}
