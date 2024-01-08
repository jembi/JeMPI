package org.jembi.jempi.libapi.mpi_stats.stats;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.server.Route;
import org.jembi.jempi.libapi.BackEnd;

public interface IMPIStat {
    Route getRoute(ActorSystem<Void> actorSystem,
                   ActorRef<BackEnd.Event> backEnd);
}
