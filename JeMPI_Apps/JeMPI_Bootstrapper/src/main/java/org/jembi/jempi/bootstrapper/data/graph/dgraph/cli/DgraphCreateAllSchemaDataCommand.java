package org.jembi.jempi.bootstrapper.data.graph.dgraph.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "createAllSchemaData", mixinStandardHelpOptions = true, description = "Create all the required "
        + "schema's and data for JeMPI Dgraph instance.")
public class DgraphCreateAllSchemaDataCommand extends BaseDgraphCommand implements Callable<Integer> {
   @Override
   public Integer call() throws Exception {
      this.init();
      return this.execute(() -> this.bootstrapper.createSchema()
            ? 0
            : 1);
   }
}
