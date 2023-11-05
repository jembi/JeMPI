package org.jembi.jempi.bootstrapper.data.sql.postgres.cli;

import picocli.CommandLine;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "resetAll")
public class PostgresResetAllCommand extends BasePostgresCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        this.init();
        return this.Execute(() -> this.bootstrapper.resetAll()  ? 0 : 1);
    }
}