package org.jembi.jempi.monitor.operations;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.http.javadsl.server.Route;
import org.jembi.jempi.monitor.lib.LibRegistry;

import java.util.function.Supplier;

import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.PathMatchers.segment;

public abstract class BaseOperation<T, E> implements IMonitorOperator {

    protected Route GetOperationsEndpoints(final String prefix, final Supplier<Route> pathFunc){
        return  pathPrefix(segment(this.GetBaseBasePrefix()).slash(segment((prefix))), pathFunc);
    }
    abstract protected Behavior<T> GetActorOperatorProcessor(LibRegistry libRegistry);
    abstract protected E GetEndPointOperatorProcessor(LibRegistry libRegistry);
    protected ActorRef<T> actorProcessor;
    protected E endPointProcessor;
    public BaseOperation(ActorContext<Void> actorContext, final LibRegistry libRegistry){
        Behavior<T> actorBehavior = this.GetActorOperatorProcessor(libRegistry);
        this.endPointProcessor = this.GetEndPointOperatorProcessor(libRegistry);

        if (this.actorProcessor != null){
            this.actorProcessor = actorContext.spawn(actorBehavior, String.format("%s-Processor",  this.GetBaseBasePrefix())); //TODO: Maybe change
            actorContext.watch(this.actorProcessor);
        }
    }
}
