package org.jembi.jempi.monitor.lib;

import org.apache.logging.log4j.Level;
import org.jembi.jempi.monitor.lib.dal.postgres.LibPostgres;
import org.jembi.jempi.monitor.lib.runnerChecker.RunnerChecker;

import java.util.Locale;

public class LibRegistry {

     public final LibPostgres postgres;
     public final RunnerChecker runnerChecker;
     public LibRegistry( final Level level,
                         final String[] dgraphHosts,
                         final int[] dgraphPorts,
                         final String sqlIP,
                         final int sqlPort,
                         final String sqlUser,
                         final String sqlPassword,
                         final String sqlDatabase,
                         final int apiPort){

          //TODO:Add try catch
          this.postgres = new LibPostgres(String.format(Locale.ROOT, "jdbc:postgresql://%s:%d/%s", sqlIP, sqlPort, sqlDatabase), sqlUser, sqlPassword);
          this.runnerChecker = new RunnerChecker(apiPort);
     }


}
