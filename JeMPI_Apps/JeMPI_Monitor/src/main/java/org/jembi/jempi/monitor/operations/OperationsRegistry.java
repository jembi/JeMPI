package org.jembi.jempi.monitor.operations;

import akka.actor.typed.javadsl.ActorContext;
import org.jembi.jempi.monitor.lib.LibRegistry;
import org.jembi.jempi.monitor.operations.data.DataOperator;
public class OperationsRegistry {

    public static IMonitorOperator[] getRegistry(final ActorContext<Void> actorContext, final LibRegistry libRegistry){

        return new IMonitorOperator[]{
                new DataOperator(actorContext, libRegistry)
        };
    }
}
