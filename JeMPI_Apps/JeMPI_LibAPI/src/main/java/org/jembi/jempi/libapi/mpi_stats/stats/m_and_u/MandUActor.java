package org.jembi.jempi.libapi.mpi_stats.stats.m_and_u;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import org.jembi.jempi.libapi.BackEnd;
import org.jembi.jempi.libmpi.LibMPI;

public class MandUActor {
    protected MandUActor() { }
    public record GetTallyMandUResponse() implements BackEnd.EventResponse { }
    public record GetTallyMandURequest(
            ActorRef<GetTallyMandUResponse> replyTo,
            String linkerId) implements BackEnd.Event {
    }

    public static Behavior<BackEnd.Event> process(final GetTallyMandURequest request, final LibMPI libMPI) {
        return  Behaviors.same();
    }
}
