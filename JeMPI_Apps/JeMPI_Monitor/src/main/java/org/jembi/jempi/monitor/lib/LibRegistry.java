package org.jembi.jempi.monitor.lib;

import org.apache.logging.log4j.Level;
import org.jembi.jempi.monitor.lib.dal.dgraph.LibDGraph;
import org.jembi.jempi.monitor.lib.dal.postgres.LibPostgres;
import org.jembi.jempi.monitor.lib.runnerChecker.RunnerChecker;

import java.util.Locale;

public class LibRegistry {
     public final LibDGraph dGraph;
     public final LibPostgres postgres;
     public final RunnerChecker runnerChecker;
     public LibRegistry(final Level level,
                         final String[] dgraphHosts,
                         final int[] dgraphPorts,
                         final int[] dgraphHttpPorts,
                         final String sqlIP,
                         final int sqlPort,
                         final String sqlUser,
                         final String sqlPassword,
                         final String sqlDatabase,
                         final int apiPort,
                         final String apiHost) {

          this.postgres = new LibPostgres(String.format(Locale.ROOT, "jdbc:postgresql://%s:%d/%s", sqlIP, sqlPort, sqlDatabase), sqlUser, sqlPassword);

          if (dgraphHosts.length != dgraphPorts.length || dgraphHttpPorts.length == 0){
               throw new ArrayIndexOutOfBoundsException("The length of dgraph hosts should match that of ports, and one host has to expose its http port");
          }
          this.dGraph = dgraphHosts.length > 0 ? new LibDGraph(dgraphHosts, dgraphPorts, dgraphHttpPorts) : null;
          this.runnerChecker = new RunnerChecker(apiHost, apiPort);
     }


}
