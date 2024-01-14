package org.jembi.jempi.bootstrapper.data.stream.kafka.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "listTopics", mixinStandardHelpOptions = true, description = "List all the topics associated with "
        + "the JeMPI instance.")
public class KafkaListTopicsCommand extends BaseKafkaCommand implements Callable<Integer> {
   @Override
   public Integer call() throws Exception {
      this.init();
      return this.execute(() -> this.bootstrapper.listTopics()
            ? 0
            : 1);
   }
}
