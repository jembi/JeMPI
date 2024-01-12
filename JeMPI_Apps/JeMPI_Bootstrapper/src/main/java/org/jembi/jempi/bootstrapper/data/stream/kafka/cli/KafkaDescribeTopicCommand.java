package org.jembi.jempi.bootstrapper.data.stream.kafka.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "describeTopic", mixinStandardHelpOptions = true, description = "Describe a topic associated with " +
                                                                                            "the JeMPI instance.")
public class KafkaDescribeTopicCommand extends BaseKafkaCommand implements Callable<Integer> {

   @CommandLine.Option(names = {"-t", "--topicName"}, description = "Topic Name", required = true)
   private String topicName;

   @Override
   public Integer call() throws Exception {
      this.init();
      return this.execute(() -> this.bootstrapper.describeTopic(topicName)
            ? 0
            : 1);
   }
}
