package org.jembi.jempi.monitor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.ActorContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.RejectionHandler;
import akka.http.javadsl.server.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libapi.BackEnd;
import org.jembi.jempi.monitor.lib.LibRegistry;
import org.jembi.jempi.monitor.operations.IMonitorOperator;
import org.jembi.jempi.monitor.operations.OperationsRegistry;

import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.concat;

public class RestHttpServer {

    private static final Logger LOGGER = LogManager.getLogger(RestHttpServer.class);
    private CompletionStage<ServerBinding> binding = null;
    private IMonitorOperator[] monitorOperators = null;
    private RestHttpServer() {
        Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
    }

    static RestHttpServer create() {
        return new RestHttpServer();
    }

    public void open(
            final String httpServerHost,
            final int httpPort,
            final ActorContext<Void> actorContext,
            LibRegistry libRegistry) {

        monitorOperators =  OperationsRegistry.getRegistry(actorContext, libRegistry);
        Http http = Http.get(actorContext.getSystem());

        final RejectionHandler rejectionHandler = RejectionHandler.defaultHandler();
        final ExceptionHandler exceptionHandler = ExceptionHandler.newBuilder()
                .match(Exception.class, x -> {
                    LOGGER.error("An exception occurred while executing the Route", x);
                    return complete(StatusCodes.INTERNAL_SERVER_ERROR, "An exception occurred. Please see server logs for details");
                }).build();

        binding = http.newServerAt(httpServerHost, httpPort).bind(this.createMonitorEndPoints().seal(rejectionHandler, exceptionHandler));
        LOGGER.info("Monitoring server running at http://{}:{}", httpServerHost, httpPort);
    }

    public void close(final ActorSystem<Void> actorSystem) {
        binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> actorSystem.terminate()); // and shutdown when done
    }


    private Route createMonitorEndPoints() {

        Route route = null;
        for (IMonitorOperator operation : monitorOperators){
            if (route == null){
                route = operation.GetEndpoints();
            } else {
                route.orElse(operation.GetEndpoints());
            }
        }
        return route;

    }
}
