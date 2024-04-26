package org.jembi.jempi.bootstrapper.data.cli;

import org.jembi.jempi.bootstrapper.data.BaseDataBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.graph.dgraph.cli.DgraphCreateAllSchemaDataCommand;
import org.jembi.jempi.bootstrapper.data.sql.postgres.cli.PostgresCreateAllSchemaDataCommand;
import org.jembi.jempi.bootstrapper.data.stream.kafka.cli.KafkaCreateAllSchemaDataCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "createAllSchemaData", mixinStandardHelpOptions = true, description = "Create all the required "
                                                                                                  + "schema's and data for "
                                                                                                  + "JeMPI.")
public class CreateAllSchemaDataCommand extends BaseCLICommand implements Callable<Integer> {
   @Override
   public Integer call() throws Exception {

      return this.execute(
            () -> this.callMultiple(new BaseDataBootstrapperCommand[]{new PostgresCreateAllSchemaDataCommand(),
                                                                      new DgraphCreateAllSchemaDataCommand(),
                                                                      new KafkaCreateAllSchemaDataCommand()}));
   }
}
