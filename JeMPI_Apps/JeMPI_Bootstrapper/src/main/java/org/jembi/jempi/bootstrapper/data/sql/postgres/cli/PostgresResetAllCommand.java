package org.jembi.jempi.bootstrapper.data.sql.postgres.cli;

import picocli.CommandLine;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "resetAll", mixinStandardHelpOptions = true, description = "Deletes all data and schemas associated with JeMPI Postgres instance, then recreated schemas, and add initial data.")
public class PostgresResetAllCommand extends BasePostgresCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        this.init();
        return this.execute(() -> this.bootstrapper.resetAll()  ? 0 : 1);
    }
}
