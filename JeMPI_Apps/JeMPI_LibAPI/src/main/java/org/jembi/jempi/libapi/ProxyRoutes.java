package org.jembi.jempi.libapi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.http.scaladsl.model.IllegalUriException;
import akka.stream.ConnectionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.ApiModels;
import org.jembi.jempi.shared.models.GlobalConstants;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;

import static akka.http.javadsl.server.Directives.*;
import static org.jembi.jempi.libapi.MapError.mapError;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class ProxyRoutes {

   private static final Logger LOGGER = LogManager.getLogger(ProxyRoutes.class);
   private static final Marshaller<Object, RequestEntity> JSON_MARSHALLER = Jackson.marshaller(OBJECT_MAPPER);


   private ProxyRoutes() {
   }

   private static CompletionStage<HttpResponse> proxyPostCalculateScoresDoIt(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCalculateScoresRequest body) throws JsonProcessingException {

      final byte[] json;
      try {
         json = OBJECT_MAPPER.writeValueAsBytes(body);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         throw e;
      }
      final var request = HttpRequest.create("http://%s:%d/JeMPI/%s".formatted(
                                           linkerIP,
                                           linkerPort,
                                           GlobalConstants.SEGMENT_PROXY_POST_CALCULATE_SCORES))
                                     .withMethod(HttpMethods.POST)
                                     .withEntity(ContentTypes.APPLICATION_JSON, json);
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   static Route proxyPostCalculateScores(
         final String linkerIp,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCalculateScoresRequest.class),
                    obj -> {
                       try {
                          return onComplete(proxyPostCalculateScoresDoIt(linkerIp, linkerPort, http, obj),
                                            response -> {
                                               if (!response.isSuccess()) {
                                                  final var e = response.failed().get();
                                                  LOGGER.error(e.getLocalizedMessage(), e);
                                                  return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                               }
                                               return complete(response.get());
                                            });
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(ApiModels.getHttpErrorResponse(StatusCodes.UNPROCESSABLE_ENTITY));
                       }
                    });
   }

   private static CompletionStage<HttpResponse> proxyPostLinkInteractionDoIt(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.LinkInteractionSyncBody body) throws JsonProcessingException {
      final HttpRequest request;
      final byte[] json;
      try {
         json = OBJECT_MAPPER.writeValueAsBytes(body);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         throw e;
      }
      request = HttpRequest.create(String.format(Locale.ROOT,
                                                 "http://%s:%d/JeMPI/%s",
                                                 linkerIP,
                                                 linkerPort,
                                                 GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION))
                           .withMethod(HttpMethods.POST)
                           .withEntity(ContentTypes.APPLICATION_JSON, json);
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   static Route proxyPostLinkInteraction(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.LinkInteractionSyncBody.class),
                    obj -> {
                       try {
                          return onComplete(proxyPostLinkInteractionDoIt(linkerIP, linkerPort, http, obj),
                                            response -> {
                                               if (!response.isSuccess()) {
                                                  final var e = response.failed().get();
                                                  LOGGER.error(e.getLocalizedMessage(), e);
                                                  return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                               }
                                               return complete(response.get());
                                            });
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(ApiModels.getHttpErrorResponse(StatusCodes.UNPROCESSABLE_ENTITY));
                       }
                    });
   }

   private static CompletionStage<HttpResponse> proxyPostCrRegisterDoIt(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCrRegisterRequest body) throws JsonProcessingException {
      final HttpRequest request;
      final byte[] json;
      try {
         json = OBJECT_MAPPER.writeValueAsBytes(body);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         throw e;
      }
      request = HttpRequest.create(String.format(Locale.ROOT,
                                                 "http://%s:%d/JeMPI/%s",
                                                 linkerIP,
                                                 linkerPort,
                                                 GlobalConstants.SEGMENT_PROXY_POST_CR_REGISTER))
                           .withMethod(HttpMethods.POST)
                           .withEntity(ContentTypes.APPLICATION_JSON, json);
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }


   static Route proxyPostCrRegister(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrRegisterRequest.class),
                    obj -> {
                       try {
                          return onComplete(proxyPostCrRegisterDoIt(linkerIP, linkerPort, http, obj),
                                            response -> {
                                               if (!response.isSuccess()) {
                                                  final var e = response.failed().get();
                                                  LOGGER.error(e.getLocalizedMessage(), e);
                                                  return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                               }
                                               return complete(response.get());
                                            });
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(ApiModels.getHttpErrorResponse(StatusCodes.UNPROCESSABLE_ENTITY));
                       }
                    });
   }

   private static CompletionStage<HttpResponse> proxyPostCrFindProxyDoIt(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCrFindRequest body) throws JsonProcessingException {
      final byte[] json;
      try {
         json = OBJECT_MAPPER.writeValueAsBytes(body);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         throw e;
      }
      final var request = HttpRequest.create(String.format(Locale.ROOT,
                                                           "http://%s:%d/JeMPI/%s",
                                                           linkerIP,
                                                           linkerPort,
                                                           GlobalConstants.SEGMENT_PROXY_POST_CR_FIND))
                                     .withMethod(HttpMethods.POST)
                                     .withEntity(ContentTypes.APPLICATION_JSON, json);
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> {
         LOGGER.debug("{}", response);
         return response;
      });
   }

   static Route proxyPostCrFind(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrFindRequest.class),
                    obj -> {
                       try {
                          return onComplete(proxyPostCrFindProxyDoIt(linkerIP, linkerPort, http, obj),
                                            response -> {
                                               if (!response.isSuccess()) {
                                                  final var e = response.failed().get();
                                                  LOGGER.error(e.getLocalizedMessage(), e);
                                                  return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                               }
                                               return complete(response.get());
                                            });
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(ApiModels.getHttpErrorResponse(StatusCodes.UNPROCESSABLE_ENTITY));
                       }
                    });
   }

   private static CompletionStage<HttpResponse> proxyPostCrCandidatesDoIt(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCrCandidatesRequest body) throws JsonProcessingException {
      final byte[] json;
      try {
         json = OBJECT_MAPPER.writeValueAsBytes(body);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         throw e;
      }
      final var request = HttpRequest.create(String.format(Locale.ROOT,
                                                           "http://%s:%d/JeMPI/%s",
                                                           linkerIP,
                                                           linkerPort,
                                                           GlobalConstants.SEGMENT_PROXY_POST_CR_CANDIDATES))
                                     .withMethod(HttpMethods.POST)
                                     .withEntity(ContentTypes.APPLICATION_JSON, json);
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> {
         LOGGER.debug("{}", response);
         return response;
      });
   }

   static Route proxyPostCrCandidates(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrCandidatesRequest.class),
                    obj -> {
                       try {
                          return onComplete(proxyPostCrCandidatesDoIt(linkerIP, linkerPort, http, obj),
                                            response -> {
                                               if (!response.isSuccess()) {
                                                  final var e = response.failed().get();
                                                  LOGGER.error(e.getLocalizedMessage(), e);
                                                  return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                               }
                                               return complete(response.get());
                                            });
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(ApiModels.getHttpErrorResponse(StatusCodes.UNPROCESSABLE_ENTITY));
                       }
                    });
   }

   private static CompletionStage<HttpResponse> proxyPostCrLinkToGidUpdateDoIt(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCrLinkToGidUpdateRequest body) throws JsonProcessingException {
      final HttpRequest request;
      final byte[] json;
      try {
         json = OBJECT_MAPPER.writeValueAsBytes(body);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         throw e;
      }
      request = HttpRequest.create(String.format(Locale.ROOT,
                                                 "http://%s:%d/JeMPI/%s",
                                                 linkerIP,
                                                 linkerPort,
                                                 GlobalConstants.SEGMENT_PROXY_POST_CR_LINK_TO_GID_UPDATE))
                           .withMethod(HttpMethods.POST)
                           .withEntity(ContentTypes.APPLICATION_JSON, json);
      return http.singleRequest(request).thenApply(response -> response);
   }

   static Route proxyPostCrLinkToGidUpdate(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrLinkToGidUpdateRequest.class),
                    obj -> {
                       try {
                          return onComplete(proxyPostCrLinkToGidUpdateDoIt(linkerIP, linkerPort, http, obj),
                                            response -> {
                                               if (!response.isSuccess()) {
                                                  final var e = response.failed().get();
                                                  LOGGER.error(e.getLocalizedMessage(), e);
                                                  return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                               }
                                               return complete(response.get());
                                            });
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(ApiModels.getHttpErrorResponse(StatusCodes.UNPROCESSABLE_ENTITY));
                       }
                    });
   }

   private static CompletionStage<HttpResponse> proxyPostCrLinkBySourceIdDoIt(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCrLinkBySourceIdRequest body) throws JsonProcessingException {
      final HttpRequest request;
      final byte[] json;
      try {
         json = OBJECT_MAPPER.writeValueAsBytes(body);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         throw e;
      }
      request = HttpRequest.create(String.format(Locale.ROOT,
                                                 "http://%s:%d/JeMPI/%s",
                                                 linkerIP,
                                                 linkerPort,
                                                 GlobalConstants.SEGMENT_PROXY_POST_CR_LINK_BY_SOURCE_ID))
                           .withMethod(HttpMethods.POST)
                           .withEntity(ContentTypes.APPLICATION_JSON, json);
      return http.singleRequest(request).thenApply(response -> response);
   }

   static Route proxyPostCrLinkBySourceId(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrLinkBySourceIdRequest.class),
                    obj -> {
                       try {
                          return onComplete(proxyPostCrLinkBySourceIdDoIt(linkerIP, linkerPort, http, obj),
                                            response -> {
                                               if (!response.isSuccess()) {
                                                  final var e = response.failed().get();
                                                  LOGGER.error(e.getLocalizedMessage(), e);
                                                  return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                               }
                                               return complete(response.get());
                                            });
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(ApiModels.getHttpErrorResponse(StatusCodes.UNPROCESSABLE_ENTITY));
                       }
                    });
   }

   private static CompletionStage<HttpResponse> proxyPostCrLinkBySourceIdUpdateDoIt(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCrLinkBySourceIdUpdateRequest body) throws JsonProcessingException {
      final HttpRequest request;
      final byte[] json;
      try {
         json = OBJECT_MAPPER.writeValueAsBytes(body);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         throw e;
      }
      request = HttpRequest.create(String.format(Locale.ROOT,
                                                 "http://%s:%d/JeMPI/%s",
                                                 linkerIP,
                                                 linkerPort,
                                                 GlobalConstants.SEGMENT_PROXY_POST_CR_LINK_BY_SOURCE_ID_UPDATE))
                           .withMethod(HttpMethods.POST)
                           .withEntity(ContentTypes.APPLICATION_JSON, json);
      return http.singleRequest(request).thenApply(response -> response);
   }

   static Route proxyPostCrLinkBySourceIdUpdate(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrLinkBySourceIdUpdateRequest.class),
                    obj -> {
                       try {
                          return onComplete(proxyPostCrLinkBySourceIdUpdateDoIt(linkerIP, linkerPort, http, obj),
                                            response -> {
                                               if (!response.isSuccess()) {
                                                  final var e = response.failed().get();
                                                  LOGGER.error(e.getLocalizedMessage(), e);
                                                  return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                               }
                                               return complete(response.get());
                                            });
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(ApiModels.getHttpErrorResponse(StatusCodes.UNPROCESSABLE_ENTITY));
                       }
                    });
   }

   private static CompletionStage<HttpResponse> proxyPatchCrUpdateFieldsDoIt(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCrUpdateFieldsRequest body) throws JsonProcessingException {
      final byte[] json;
      try {
         json = OBJECT_MAPPER.writeValueAsBytes(body);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         throw e;
      }
      final var request = HttpRequest.create(String.format(Locale.ROOT,
                                                           "http://%s:%d/JeMPI/%s",
                                                           linkerIP,
                                                           linkerPort,
                                                           GlobalConstants.SEGMENT_PROXY_PATCH_CR_UPDATE_FIELDS))
                                     .withMethod(HttpMethods.PATCH)
                                     .withEntity(ContentTypes.APPLICATION_JSON, json);
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   static Route proxyPatchCrUpdateFields(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCrUpdateFieldsRequest.class),
                    obj -> {
                       try {
                          return onComplete(proxyPatchCrUpdateFieldsDoIt(linkerIP, linkerPort, http, obj),
                                            response -> {
                                               if (!response.isSuccess()) {
                                                  final var e = response.failed().get();
                                                  LOGGER.error(e.getLocalizedMessage(), e);
                                                  return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                               }
                                               return complete(response.get());
                                            });
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(ApiModels.getHttpErrorResponse(StatusCodes.UNPROCESSABLE_ENTITY));
                       }
                    });
   }

   private static CompletionStage<HttpResponse> proxyGetCandidatesWithScoreDoIt(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final String iid) {
      final var uri = String.format(Locale.ROOT,
                                    "http://%s:%d/JeMPI/%s?iid=%s",
                                    linkerIP,
                                    linkerPort,
                                    GlobalConstants.SEGMENT_PROXY_GET_CANDIDATES_WITH_SCORES,
                                    iid);
      final var request = HttpRequest.create(uri);
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   static Route proxyGetCandidatesWithScore(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return parameter("iid",
                       iid -> onComplete(proxyGetCandidatesWithScoreDoIt(linkerIP, linkerPort, http, iid),
                                         response -> {
                                            if (!response.isSuccess()) {
                                               final var e = response.failed().get();
                                               if (e instanceof IllegalUriException illegalUriException) {
                                                  LOGGER.error(illegalUriException.getLocalizedMessage(), illegalUriException);
                                               } else {
                                                  LOGGER.error(e.getLocalizedMessage(), e);
                                               }
                                               return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                            }
                                            return complete(response.get());
                                         }));
   }

   static Route proxyGetDashboardData(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String controllerIp,
         final Integer controllerPort,
         final Http http) {

      final var uri = String.format(Locale.ROOT,
                                    "http://%s:%d/JeMPI/%s",
                                    controllerIp,
                                    controllerPort,
                                    GlobalConstants.SEGMENT_PROXY_GET_DASHBOARD_DATA);
      final var request = HttpRequest.create(uri)
                                     .withMethod(HttpMethods.GET)
                                     .withEntity(akka.http.scaladsl.model.HttpEntity.Empty());

      final CompletableFuture<BackEnd.SQLDashboardDataResponse> sqlDashboardDataFuture = Ask.getSQLDashboardData(actorSystem,
                                                                                                                 backEnd)
                                                                                            .toCompletableFuture();

      final CompletableFuture<HttpResponse> dashboardDataFuture = http.singleRequest(request).toCompletableFuture();

      return onComplete(CompletableFuture.allOf(sqlDashboardDataFuture, dashboardDataFuture),
                        result -> {
                           if (result.isSuccess()) {
                              final HttpResponse dashboardDataResponse = dashboardDataFuture.join();
                              if (dashboardDataResponse.status() != StatusCodes.OK) {
                                 LOGGER.error("Error getting dashboard data ");
                                 return complete(StatusCodes.INTERNAL_SERVER_ERROR);
                              }
                              final String responseBody;
                              try {
                                 responseBody = Unmarshaller
                                       .entityToString()
                                       .unmarshal(dashboardDataResponse.entity(), actorSystem)
                                       .toCompletableFuture().get(10, TimeUnit.SECONDS);
                              } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                 LOGGER.error("Error getting dashboard data ", e);
                                 return complete(StatusCodes.INTERNAL_SERVER_ERROR);
                              }

                              final Map<String, Object> dashboardDataResults = Map
                                    .ofEntries(Map.entry("sqlDashboardData", sqlDashboardDataFuture.join()),
                                               Map.entry("dashboardData", responseBody));

                              return complete(StatusCodes.OK, dashboardDataResults, JSON_MARSHALLER);
                           } else {
                              final var e = result.failed().get();
                              if (e instanceof IllegalUriException illegalUriException) {
                                 LOGGER.error(illegalUriException.getLocalizedMessage(), illegalUriException);
                              } else if (e instanceof ConnectionException connectionException) {
                                 LOGGER.error(connectionException.getLocalizedMessage(), connectionException);
                                 final var cause = connectionException.getCause();
                                 LOGGER.error(cause.getLocalizedMessage(), cause);
                              } else {
                                 LOGGER.error(e.getLocalizedMessage(), e);
                              }
                              LOGGER.error(e.getLocalizedMessage(), e);
                              return complete(StatusCodes.INTERNAL_SERVER_ERROR);
                           }
                        });
   }

}
