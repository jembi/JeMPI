package org.jembi.jempi.monitor.operations.data;


import akka.actor.typed.Behavior;
import org.jembi.jempi.monitor.lib.LibRegistry;
import org.jembi.jempi.monitor.operations.BaseOperation;

import akka.http.javadsl.server.Route;
import static akka.http.javadsl.server.Directives.*;
import akka.actor.typed.javadsl.ActorContext;

public class DataOperator extends BaseOperation<Void, DataEndPontProcessor> {

    static class EndPointPaths{
        public static final String DELETE = "delete";
    }

    @Override
    protected Behavior<Void> GetActorOperatorProcessor(LibRegistry libRegistry) {
        return null;
    }

    @Override
    protected DataEndPontProcessor GetEndPointOperatorProcessor(LibRegistry libRegistry) {
        return new DataEndPontProcessor();
    }

    public DataOperator(ActorContext<Void> actorContext, LibRegistry libRegistry) {
        super(actorContext, libRegistry);
    }

    @Override
    public Route GetEndpoints() {

        return concat(get(() -> concat(
                                        path(this.GetOperationsEndpoints(EndPointPaths.DELETE),
                                                () -> {
                                                    this.endPointProcessor.deleteAll();
                                                    return complete("ssss");
                                                }))));
    }

    @Override
    public String GetBaseBasePrefix() {
        return "data";
    }
}
