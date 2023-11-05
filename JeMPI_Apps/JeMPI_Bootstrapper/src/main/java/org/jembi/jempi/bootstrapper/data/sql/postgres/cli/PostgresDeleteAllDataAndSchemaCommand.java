package org.jembi.jempi.bootstrapper.data.sql.postgres.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteAllDataAndSchemas")
public class PostgresDeleteAllDataAndSchemaCommand extends BasePostgresCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        this.init();
        return this.Execute(() -> this.bootstrapper.deleteData() && this.bootstrapper.deleteTables() ? 0 : 1);
    }
}
