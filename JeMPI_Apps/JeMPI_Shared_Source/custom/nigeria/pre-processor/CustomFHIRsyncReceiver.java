package org.jembi.jempi.pre_processor;

import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.util.BundleUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r4.model.*;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.models.LinkEntitySyncBody;
import org.jembi.jempi.shared.models.SourceId;
import org.jembi.jempi.shared.utils.AppUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class CustomFHIRsyncReceiver extends AllDirectives {

   private static final Logger LOGGER = LogManager.getLogger(CustomFHIRsyncReceiver.class);
   private static final String PATTERN = "yyyy-MM-dd";
   private static final DateFormat DF = new SimpleDateFormat(PATTERN);
   private final FhirContext ctx = FhirContext.forR4();
   private final IParser parser = ctx.newJsonParser();

   private CompletionStage<ServerBinding> binding = null;
   private Http http = null;

   void close(ActorSystem<Void> system) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> system.terminate()); // and shutdown when done
   }

   void open(final ActorSystem<Void> system) {
      http = Http.get(system);
      binding = http.newServerAt(AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT)
                    .bind(this.createRoute());
      LOGGER.info("Server online at http://{}:{}", AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT);
   }

   private CompletionStage<HttpResponse> postLinkEntity(final String json) {
      LOGGER.debug("json : {}", json);
      final var request = HttpRequest
            .create("http://jempi-controller:50000/JeMPI/link_entity")
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, json);
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private Route routeLinkEntity() {
      return entity(Unmarshaller.entityToString(),
                    json -> {
                       LOGGER.debug("{}", json);
                       final var bundle = parser.parseResource(Bundle.class, json);
                       final var patients = BundleUtil.toListOfResourcesOfType(ctx, bundle, Patient.class);
                       final var patient = patients.get(0);
                       LOGGER.debug("{}", parser.encodeResourceToString(patient));
                       final HumanName name = patient.hasName() ? patient.getName().get(0) : null;
                       final String gender = patient.hasGender() ? patient.getGender().toCode() : null;
                       final Address address = patient.hasAddress() ? patient.getAddress().get(0) : null;
                       final String city = address != null ? address.hasCity() ? address.getCity() : null : null;
                       final String dob = patient.hasBirthDate() ? DF.format(patient.getBirthDate()) : null;
                       final List<ContactPoint> telecomList = patient.hasTelecom() ? patient.getTelecom() : null;
                       final ContactPoint contactPoint = telecomList != null ? telecomList.get(0) : null;
                       final String phone = contactPoint != null ? contactPoint.getValue() : null;
                       final List<Identifier> identifierList = patient.getIdentifier();
                       String secondaryID = null;
                       for (Identifier identifier : identifierList) {
                          if (Identifier.IdentifierUse.SECONDARY.equals(identifier.getUse())) {
                             secondaryID = identifier.getValue();
                          }
                       }
                       final String givenName = name != null && name.hasGiven() ? name.getGiven().get(0).getValue() : null;
                       final String familyName = name != null ? name.getFamily() : null;
                       final var customEntity = new CustomEntity();
                       LOGGER.debug("{}", customEntity);
                       try {
                          final var linkEntitySyncBody = new LinkEntitySyncBody(secondaryID, null, 0.65F, customEntity);
                          final var jsonOut = AppUtils.OBJECT_MAPPER.writeValueAsString(linkEntitySyncBody);
                          return onComplete(postLinkEntity(jsonOut), response -> response.isSuccess()
                                                                                 ? complete(response.get())
                                                                                 : complete(StatusCodes.IM_A_TEAPOT));
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }

                    });
   }

   private Route createRoute() {
      return pathPrefix("fhir",
                        () -> concat(
                              post(() -> concat(
                                    path("bundle", this::routeLinkEntity)))
                                    )
                       );
   }

}
