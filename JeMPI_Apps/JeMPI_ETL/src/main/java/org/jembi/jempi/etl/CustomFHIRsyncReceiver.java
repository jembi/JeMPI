package org.jembi.jempi.etl;

import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.stream.Materializer;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.util.BundleUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r4.model.*;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;
import scala.concurrent.duration.FiniteDuration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public final class CustomFHIRsyncReceiver extends AllDirectives {

   private static final Logger LOGGER = LogManager.getLogger(CustomFHIRsyncReceiver.class);
   private static final String PATTERN = "yyyy-MM-dd";
   private static final DateFormat DF = new SimpleDateFormat(PATTERN);
   private final FhirContext ctx = FhirContext.forR4();
   private final IParser parser = ctx.newJsonParser();

   private CompletionStage<ServerBinding> binding = null;
   private Http http = null;


   void close(final ActorSystem<Void> system) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> system.terminate()); // and shutdown when done
   }

   void open(final ActorSystem<Void> system) {
      final Materializer materializer = Materializer.createMaterializer(system);
      http = Http.get(system);
      binding = http.newServerAt(AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT)
                    .bind(this.createRoute(system, materializer));
      LOGGER.info("Server online at http://{}:{}", AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT);
   }

   private CompletionStage<HttpResponse> postLinkPatient(
         final ActorSystem<Void> system,
         final String json) {
      LOGGER.debug("json : {}", json);
      final var request = HttpRequest
            .create("http://jempi-controller:50000/JeMPI/link_patient")
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, json);
      return http.singleRequest(request)
                 .thenCompose(
                       response -> response
                             .entity()
                             .toStrict(FiniteDuration.create(3, TimeUnit.SECONDS).toMillis(), system)
                             .thenApply(strict -> {
                                final var jsonResponse = strict.getData().utf8String();
                                LOGGER.debug("json: {}", jsonResponse);
                                ExtendedLinkInfo extendedLinkInfo = null;
                                try {
                                   extendedLinkInfo = AppUtils.OBJECT_MAPPER.readValue(jsonResponse, ExtendedLinkInfo.class);
                                } catch (JsonProcessingException e) {
                                   LOGGER.error(e.getLocalizedMessage(), e);
                                   return HttpResponse.create().withStatus(StatusCodes.IM_A_TEAPOT);
                                }
                                LOGGER.debug("{}", extendedLinkInfo);
                                final var patient = new Patient();
                                final var patientLinkComponent = new Patient.PatientLinkComponent();
                                patientLinkComponent.setOther(new Reference(extendedLinkInfo.linkInfo().patientUID()));
                                patient.addLink(patientLinkComponent);
                                final var jsonFHIR = parser.encodeResourceToString(patient);
                                return HttpResponse.create()
                                                   .withEntity(ContentTypes.APPLICATION_JSON, jsonFHIR)
                                                   .withStatus(StatusCodes.OK);
                             }));
   }

   private Route routeLinkPatient(
         final ActorSystem<Void> system,
         final Materializer materializer) {
      return entity(Unmarshaller.entityToString(),
                    json -> {
                       LOGGER.debug("{}", json);
                       final var bundle = parser.parseResource(Bundle.class, json);
                       final var patients = BundleUtil.toListOfResourcesOfType(ctx, bundle, Patient.class);
                       final var patient = patients.get(0);
                       LOGGER.debug("{}", parser.encodeResourceToString(patient));
                       final HumanName name = patient.hasName()
                             ? patient.getName().get(0)
                             : null;
                       final String gender = patient.hasGender()
                             ? patient.getGender().toCode()
                             : null;
                       final Address address = patient.hasAddress()
                             ? patient.getAddress().get(0)
                             : null;
                       final String city = address != null
                             ? (address.hasCity()
                                      ? address.getCity()
                                      : null)
                             : null;
                       final String dob = patient.hasBirthDate()
                             ? DF.format(patient.getBirthDate())
                             : null;
                       final List<ContactPoint> telecomList = patient.hasTelecom()
                             ? patient.getTelecom()
                             : null;
                       final ContactPoint contactPoint = telecomList != null
                             ? telecomList.get(0)
                             : null;
                       final String phone = contactPoint != null
                             ? contactPoint.getValue()
                             : null;
                       final List<Identifier> identifierList = patient.getIdentifier();
                       String secondaryID = null;
                       String officialID = null;
                       SourceId sourceId = null;
                       for (Identifier identifier : identifierList) {
                          if (Identifier.IdentifierUse.SECONDARY.equals(identifier.getUse())) {
                             secondaryID = identifier.getValue();
                          } else if (Identifier.IdentifierUse.OFFICIAL.equals(identifier.getUse())) {
                             officialID = identifier.getValue();
                          } else if (identifier.getSystem().startsWith("http://jempi.org/fhir/identifier/facility_")) {
                             sourceId = new SourceId(
                                   null,
                                   identifier.getSystem().substring("http://jempi.org/fhir/identifier/facility_".length()),
                                   identifier.getValue()
                             );
                          }
                       }
                       final String givenName = name != null && name.hasGiven()
                             ? name.getGiven().get(0).getValue()
                             : null;
                       final String familyName = name != null
                             ? name.getFamily()
                             : null;
                       final var customPatient = new PatientRecord(null,
                                                                   sourceId,
                                                                   new CustomDemographicData(secondaryID,
                                                                                             givenName, familyName, gender, dob,
                                                                                             city, phone, officialID));
                       LOGGER.debug("{}", customPatient);
                       try {
                          final var jsonIn = AppUtils.OBJECT_MAPPER.writeValueAsString(
                                new LinkPatientSyncBody(secondaryID, null, 0.65F, customPatient));
                          return onComplete(postLinkPatient(system, jsonIn),
                                            response -> {
                                               if (response.isSuccess()) {
                                                  return complete(response.get());
                                               } else {
                                                  return complete(StatusCodes.IM_A_TEAPOT);
                                               }
                                            });
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }

                    });
   }

   private Route createRoute(
         final ActorSystem<Void> system,
         final Materializer materializer) {
      return pathPrefix("fhir",
                        () -> concat(
                              post(() -> routeLinkPatient(system, materializer))));
   }

}
