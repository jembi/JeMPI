package org.jembi.jempi.bootstrapper.data.sql.postgres.cli;

import picocli.CommandLine;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteDataOnly", mixinStandardHelpOptions = true, description = "Delete all the data (only) used by JeMPI Postgres instance.")
public class PostgresDeleteDataOnlyCommand extends BasePostgresCommand implements Callable<Integer>{
    @Override
    public Integer call() throws Exception {
        this.init();
        return this.Execute(() -> this.bootstrapper.deleteData()  ? 0 : 1);
    }
}