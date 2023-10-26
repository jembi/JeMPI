package org.jembi.jempi.monitor.operations;

import akka.http.javadsl.server.Route;
public interface IMonitorOperator {
    Route GetEndpoints();
    String GetBaseBasePrefix();
}
