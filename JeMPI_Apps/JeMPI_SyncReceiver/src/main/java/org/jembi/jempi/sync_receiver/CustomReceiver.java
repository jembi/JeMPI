package org.jembi.jempi.sync_receiver;

import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;

import java.util.concurrent.CompletionStage;

class CustomReceiver extends AllDirectives {

    private static final Logger LOGGER = LogManager.getLogger(CustomReceiver.class);
    private CompletionStage<ServerBinding> binding = null;
    private Http http = null;

    void close(final ActorSystem<Void> system) {
        binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    void open(final ActorSystem<Void> system) {
        http = Http.get(system);
        binding = http.newServerAt(AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT)
                .bind(this.createRoute());
        LOGGER.info("Server online at http://{}:{}", AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT);
    }

    private CompletionStage<HttpResponse> postLinkPatient(final String json) {
        LOGGER.debug("{}", json);
        final HttpRequest request;
        request = HttpRequest
                .create("http://jempi-etl:50000/fhir")
                .withMethod(HttpMethods.POST)
                .withEntity(ContentTypes.APPLICATION_JSON, json);
        final var stage = http.singleRequest(request);
        return stage.thenApply(response -> response);
    }

    private Route routeLinkPatient() {
        return entity(Unmarshaller.entityToString(),
                json -> {
                    LOGGER.debug("{}", json);
                    return onComplete(postLinkPatient(json), response -> response.isSuccess()
                            ? complete(response.get())
                            : complete(StatusCodes.IM_A_TEAPOT));
                }
        );
    }

    private Route createRoute() {
        return pathPrefix("fhir",
                () -> concat(
                        post(this::routeLinkPatient)));
    }

}
