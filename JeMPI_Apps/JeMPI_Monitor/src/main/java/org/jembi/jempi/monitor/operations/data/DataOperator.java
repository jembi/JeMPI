package org.jembi.jempi.monitor.operations.data;


import akka.actor.typed.Behavior;
import org.jembi.jempi.monitor.lib.LibRegistry;
import org.jembi.jempi.monitor.operations.BaseOperation;

import akka.http.javadsl.server.Route;
import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.PathMatchers.segment;

import akka.actor.typed.javadsl.ActorContext;

import java.util.regex.Pattern;

public class DataOperator extends BaseOperation<Void, DataEndPointProcessor> {

    static class EndPointPaths{
        public static final String DELETE = "delete";
    }

    @Override
    protected Behavior<Void> GetActorOperatorProcessor(LibRegistry libRegistry) {
        return null;
    }

    @Override
    protected DataEndPointProcessor GetEndPointOperatorProcessor(LibRegistry libRegistry) {
        return new DataEndPointProcessor(libRegistry);
    }

    public DataOperator(ActorContext<Void> actorContext, LibRegistry libRegistry) {
        super(actorContext, libRegistry);
    }

    @Override
    public Route GetEndpoints() {

        // /data/delete/^(postgres|dgraph)$/(__all|[a-zA-Z][a-zA-Z0-9_]*)/?\?force
        return concat(get(() -> concat(this.GetOperationsEndpoints(EndPointPaths.DELETE ,
                                    () -> pathPrefix(segment(Pattern.compile("^(postgres|dgraph)$")),
                                            dbType -> path(segment(Pattern.compile("(__all|[a-zA-Z][a-zA-Z0-9_]*)")),
                                                    tableName -> parameterOptional("force", force ->
                                                             this.endPointProcessor.deleteAll(dbType, tableName, force.isPresent()


        ))))))));

    }

    @Override
    public String GetBaseBasePrefix() {
        return "data";
    }
}

