package org.jembi.jempi.libapi.mpi_stats.stats;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.server.Route;
import org.jembi.jempi.libapi.BackEnd;

public interface IMPIStat {
    Route getRoute(final ActorSystem<Void> actorSystem,
                   final ActorRef<BackEnd.Event> backEnd);
}
