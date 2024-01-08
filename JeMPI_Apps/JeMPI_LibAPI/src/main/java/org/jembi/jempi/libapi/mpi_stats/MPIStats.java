package org.jembi.jempi.libapi.mpi_stats;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.server.Route;
import org.jembi.jempi.libapi.BackEnd;
import org.jembi.jempi.libapi.mpi_stats.stats.IMPIStat;
import org.jembi.jempi.libapi.mpi_stats.stats.m_and_u.MandU;

import java.util.List;

import static akka.http.javadsl.server.Directives.concat;
import static akka.http.javadsl.server.Directives.pathPrefix;

public class MPIStats {
    protected MPIStats() { }
    private static final List<IMPIStat> STATS_REGISTRY = List.of(new MandU());
    public static Route getRoutes(final ActorSystem<Void> actorSystem,
                                  final ActorRef<BackEnd.Event> backEnd) {

        Route statRoutes = null;

        for (IMPIStat stat: STATS_REGISTRY) {
            statRoutes = statRoutes == null ? stat.getRoute(actorSystem, backEnd) : concat(statRoutes, stat.getRoute(actorSystem, backEnd));
        }

        Route finalStatRoutes = statRoutes;
        return pathPrefix("mpi_stats", () -> finalStatRoutes);
    }
}
