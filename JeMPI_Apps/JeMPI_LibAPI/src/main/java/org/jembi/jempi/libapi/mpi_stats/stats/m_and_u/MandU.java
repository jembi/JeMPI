package org.jembi.jempi.libapi.mpi_stats.stats.m_and_u;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import org.jembi.jempi.libapi.BackEnd;
import org.jembi.jempi.libapi.mpi_stats.stats.IMPIStat;

import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.*;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final  class MandU implements IMPIStat {
    private static final Marshaller<Object, RequestEntity> JSON_MARSHALLER = Jackson.marshaller(OBJECT_MAPPER);
    public static final String ROUTE_NAME = "m_and_u";
    public Route getRoute(final ActorSystem<Void> actorSystem,
                          final ActorRef<BackEnd.Event> backEnd) {
        return get(() -> path(MandU.ROUTE_NAME, () -> this.handler(actorSystem, backEnd, "")));
    }

    private Route handler(final ActorSystem<Void> actorSystem,
                          final ActorRef<BackEnd.Event> backEnd, final String linkerId) {

        CompletionStage<MandUActor.GetTallyMandUResponse> stage = AskPattern
                .ask(backEnd,
                        replyTo -> new MandUActor.GetTallyMandURequest(replyTo, linkerId),
                        java.time.Duration.ofSeconds(6),
                        actorSystem.scheduler());

        return onComplete(stage, result ->  {
            if (result.isSuccess()) {
                Object processedResult = result.get();
                return complete(StatusCodes.OK, processedResult, JSON_MARSHALLER);
            }
            return complete(StatusCodes.INTERNAL_SERVER_ERROR);
        });

    }
}
