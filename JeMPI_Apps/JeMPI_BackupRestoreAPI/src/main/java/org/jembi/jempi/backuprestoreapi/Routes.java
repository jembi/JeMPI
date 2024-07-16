package org.jembi.jempi.backuprestoreapi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.ApiModels;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.RestoreGoldenRecords;

import java.util.Map;
import java.util.function.Function;

import static akka.http.javadsl.server.Directives.*;
import static org.jembi.jempi.backuprestoreapi.MapError.mapError;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class Routes {

   private static final Logger LOGGER = LogManager.getLogger(Routes.class);
   private static final Marshaller<Object, RequestEntity> JSON_MARSHALLER = Jackson.marshaller(OBJECT_MAPPER);
   private static final Function<Map.Entry<String, String>, String> PARAM_STRING = Map.Entry::getValue;

   private Routes() {
   }

    private static Route getGidsAll(
            final ActorSystem<Void> actorSystem,
            final ActorRef<BackEnd.Event> backEnd) {
        return onComplete(Ask.getGidsAll(actorSystem, backEnd),
                result -> {
                    if (!result.isSuccess()) {
                        final var e = result.failed().get();
                        LOGGER.error(e.getLocalizedMessage(), e);
                        return mapError(new MpiServiceError.InternalError(
                                e.getLocalizedMessage()));
                    }
                    return complete(StatusCodes.OK, result.get(), JSON_MARSHALLER);
                });
    }

    private static Route postGoldenRecord(
            final ActorSystem<Void> actorSystem,
            final ActorRef<BackEnd.Event> backEnd) {
        return entity(Jackson.unmarshaller(RestoreGoldenRecords.class),
                payload -> payload != null
                        ? onComplete(Ask.postGoldenRecord(actorSystem, backEnd, payload),
                        result -> {
                            if (result.isSuccess()) {
                                final var updatedFields = result.get().goldenRecord();
                                if (updatedFields.isEmpty()) {
                                    return complete(StatusCodes.BAD_REQUEST);
                                } else {
                                    return complete(StatusCodes.OK,
                                            result.get(),
                                            JSON_MARSHALLER);
                                }
                            } else {
                                return complete(StatusCodes.INTERNAL_SERVER_ERROR);
                            }
                        })
                        : complete(StatusCodes.NO_CONTENT));
    }

    private static Route getExpandedGoldenRecord(
            final ActorSystem<Void> actorSystem,
            final ActorRef<BackEnd.Event> backEnd) {
       return entity(Jackson.unmarshaller(ApiModels.ApiGoldenRecords.class),
                     request -> onComplete(Ask.getExpandedGoldenRecord(actorSystem, backEnd, request),
                                           result -> {
                                              if (!result.isSuccess()) {
                                                 final var e = result.failed().get();
                                                 LOGGER.error(e.getLocalizedMessage(), e);
                                                 return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                              }
                                              return result.get()
                                                           .goldenRecord()
                                                           .mapLeft(MapError::mapError)
                                                           .fold(error -> error,
                                                                 goldenRecord -> complete(StatusCodes.OK,
                                                                                          ApiModels.ApiExpandedGoldenRecord
                                                                                                .fromExpandedGoldenRecord(
                                                                                                      goldenRecord),
                                                                                          Jackson.marshaller(OBJECT_MAPPER)));
                                           }));
    }

    public static Route createCoreAPIRoutes(
            final ActorSystem<Void> actorSystem,
            final ActorRef<BackEnd.Event> backEnd
    ) {
        return concat(post(() -> concat(
                        path(GlobalConstants.SEGMENT_POST_EXPANDED_GOLDEN_RECORD,
                                () -> Routes.getExpandedGoldenRecord(actorSystem, backEnd)),
                        path(GlobalConstants.SEGMENT_POST_GOLDEN_RECORD_RESTORE,
                                () -> Routes.postGoldenRecord(actorSystem, backEnd))
                )),
                get(() -> concat(
                        path(GlobalConstants.SEGMENT_GET_GIDS_ALL,
                                () -> Routes.getGidsAll(actorSystem, backEnd))
                )));
    }

}
