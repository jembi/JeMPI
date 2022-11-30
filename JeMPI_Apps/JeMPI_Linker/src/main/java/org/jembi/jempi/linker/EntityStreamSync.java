package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.ExtendedLinkInfo;
import org.jembi.jempi.shared.models.LinkEntitySyncBody;
import org.jembi.jempi.shared.models.LinkEntityToGidSyncBody;

import java.util.concurrent.CompletionStage;

class EntityStreamSync extends AllDirectives {

    private static final Logger LOGGER = LogManager.getLogger(EntityStreamSync.class);
    private CompletionStage<ServerBinding> binding = null;

    private EntityStreamSync() {
    }

    static EntityStreamSync create() {
        return new EntityStreamSync();
    }

    void close(ActorSystem<Void> system) {
        binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    void open(final ActorSystem<Void> system,
              final ActorRef<BackEnd.Event> backEnd) {
        final Http http = Http.get(system);
        binding = http.newServerAt(AppConfig.HTTP_SERVER_HOST,
                        AppConfig.HTTP_SERVER_PORT)
                .bind(this.createRoute(system, backEnd));
        LOGGER.info("Server online at http://{}:{}", AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT);
    }

    private CompletionStage<BackEnd.EventLinkEntitySyncRsp> postLinkEntity(final ActorSystem<Void> actorSystem,
                                                                           final ActorRef<BackEnd.Event> backEnd,
                                                                           final LinkEntitySyncBody body) {
        CompletionStage<BackEnd.EventLinkEntitySyncRsp> stage =
                AskPattern.ask(backEnd,
                        replyTo -> new BackEnd.EventLinkEntitySyncReq(body, replyTo),
                        java.time.Duration.ofSeconds(11),
                        actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventLinkEntityToGidSyncRsp> postLinkEntityToGid(final ActorSystem<Void> actorSystem,
                                                                                     final ActorRef<BackEnd.Event> backEnd,
                                                                                     final LinkEntityToGidSyncBody body) {
        CompletionStage<BackEnd.EventLinkEntityToGidSyncRsp> stage =
                AskPattern.ask(backEnd,
                        replyTo -> new BackEnd.EventLinkEntityToGidSyncReq(body, replyTo),
                        java.time.Duration.ofSeconds(11),
                        actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }


    private CompletionStage<BackEnd.EventGetMURsp> getMU(final ActorSystem<Void> actorSystem,
                                                         final ActorRef<BackEnd.Event> backEnd) {
        CompletionStage<BackEnd.EventGetMURsp> stage =
                AskPattern.ask(backEnd,
                        BackEnd.EventGetMUReq::new,
                        java.time.Duration.ofSeconds(11),
                        actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private Route routeLinkEntity(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return entity(Jackson.unmarshaller(LinkEntitySyncBody.class),
                obj -> onComplete(postLinkEntity(actorSystem, backEnd, obj),
                        response -> {
                            if (response.isSuccess()) {
                                final var eventLinkEntitySyncRsp = response.get();
                                return complete(
                                        StatusCodes.OK,
                                        new ExtendedLinkInfo(
                                                eventLinkEntitySyncRsp.stan(),
                                                eventLinkEntitySyncRsp.linkInfo(),
                                                eventLinkEntitySyncRsp.externalLinkCandidateList()),
                                        Jackson.marshaller());
                            } else {
                                return complete(StatusCodes.IM_A_TEAPOT);
                            }
                        }));
    }

    private Route routeLinkEntityToGid(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return entity(Jackson.unmarshaller(LinkEntityToGidSyncBody.class),
                obj -> onComplete(
                        postLinkEntityToGid(actorSystem, backEnd, obj),
                        response -> response.isSuccess()
                                ? complete(StatusCodes.OK, response.get(), Jackson.marshaller())
                                : complete(StatusCodes.IM_A_TEAPOT)));
    }

    private Route routeMU(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return onComplete(getMU(actorSystem, backEnd),
                response -> response.isSuccess()
                        ? complete(StatusCodes.OK, response.get().mu(), Jackson.marshaller())
                        : complete(StatusCodes.IM_A_TEAPOT));
    }

    private Route createRoute(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return pathPrefix("JeMPI",
                () -> concat(
                        post(() -> concat(
                                path("link_entity", () -> routeLinkEntity(actorSystem, backEnd)),
                                path("link_entity_to_gid", () -> routeLinkEntityToGid(actorSystem, backEnd)))),
                        get(() -> concat(
                                path("mu", () -> routeMU(actorSystem, backEnd))))));
    }

}
