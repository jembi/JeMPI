package org.jembi.jempi.api.httpServer.httpServerRoutes.routes;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.api.httpServer.HttpServer;
import org.jembi.jempi.api.httpServer.httpServerRoutes.ApiHttpServerRouteEntries;
import org.jembi.jempi.libapi.Ask;
import org.jembi.jempi.libapi.BackEnd;

import org.jembi.jempi.shared.models.GlobalConstants;

import java.io.File;

import static akka.http.javadsl.server.Directives.*;


public class AdminRoutes extends ApiHttpServerRouteEntries {

    private static final Logger LOGGER = LogManager.getLogger(AdminRoutes.class);
    public AdminRoutes(HttpServer ihttpServer) {
        super(ihttpServer);
    }

    private Route postUploadCsvFile(
                final ActorSystem<Void> actorSystem,
                final ActorRef<BackEnd.Event> backEnd) {
            return withSizeLimit(1024L * 1024L * 10L,
                    () -> this.httpServer.requiredSession(this.sessionManager.getRefreshable(), this.sessionManager.getSessionTransport(), session -> {
                        if (session != null) {
                            LOGGER.info("Current session: {}", session.getEmail());
                            return storeUploadedFile("csv",
                                    info -> {
                                        try {
                                            return File.createTempFile("import-", ".csv");
                                        } catch (Exception e) {
                                            LOGGER.error("error", e);
                                            return null;
                                        }
                                    },
                                    (info, file) -> onComplete(Ask.postUploadCsvFile(actorSystem, backEnd,
                                            info, file),
                                            response -> response.isSuccess()
                                                    ? complete(StatusCodes.OK)
                                                    : complete(StatusCodes.IM_A_TEAPOT)));
                        }
                        LOGGER.info("No active session");
                        return complete(StatusCodes.FORBIDDEN);
                    }));
        }


    @Override
    public Route getRouteEntries() {
        final ActorSystem<Void> actorSystem = this.httpServer.getActorSystem();
        final ActorRef<BackEnd.Event> backEnd = this.httpServer.getBackEnd();
        return post(() -> concat(
                    path(GlobalConstants.SEGMENT_POST_UPLOAD_CSV_FILE,() -> this.postUploadCsvFile(actorSystem, backEnd))
                ));
    }
}
