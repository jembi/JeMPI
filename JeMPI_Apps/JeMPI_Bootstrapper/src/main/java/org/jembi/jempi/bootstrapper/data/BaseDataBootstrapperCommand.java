package org.jembi.jempi.bootstrapper.data;

import org.apache.logging.log4j.Logger;
import org.jembi.jempi.bootstrapper.data.cli.CreateAllSchemaDataCommand;
import org.jembi.jempi.bootstrapper.data.cli.DeleteAllSchemaDataCommand;
import org.jembi.jempi.bootstrapper.data.cli.ResetAllCommand;
import org.jembi.jempi.bootstrapper.data.graph.dgraph.cli.DgraphCLI;
import org.jembi.jempi.bootstrapper.data.sql.postgres.cli.PostgresCLI;
import org.jembi.jempi.bootstrapper.data.stream.kafka.cli.KafkaCLI;
import org.jembi.jempi.bootstrapper.utils.BootstrapperLogger;
import picocli.CommandLine;

import java.util.concurrent.Callable;

public abstract class BaseDataBootstrapperCommand<T extends DataBootstrapper> implements Callable<Integer> {
    protected static final Logger LOGGER = BootstrapperLogger.getLogger( "DataBootstrapperCLI");

    @CommandLine.Option(names = "config", scope = CommandLine.ScopeType.INHERIT)
    protected String config;

    protected T bootstrapper;

    public BaseDataBootstrapperCommand<T> init() throws Exception{
        bootstrapper = getBootstrapper(config);
        return this;
    }

    protected Integer Execute(Callable<Integer> bootstrapperFunc){
        try{
            Integer bootstrapperResult = bootstrapperFunc.call();
            if (bootstrapperResult != 0 ){
                LOGGER.warn("Command completed successfully with some errors");
                return CommandLine.ExitCode.SOFTWARE;
            }
            return CommandLine.ExitCode.OK;
        } catch (Exception e){
            LOGGER.error("An error occurred whilst executing the command.", e);
            return CommandLine.ExitCode.SOFTWARE;
        }
    }
    protected abstract T getBootstrapper(String configPath) throws Exception;
    public BaseDataBootstrapperCommand<T> setConfigPath(String config){
        this.config = config;
        return this;
    }
}
