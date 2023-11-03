package org.jembi.jempi.bootstrapper.data.stream.kafka.cli;

import picocli.CommandLine.Command;

@Command(
        name = "kafka",
        mixinStandardHelpOptions = true,
        subcommands = {
                KafkaResetAllCommand.class,
                KafkaDeleteAllDataCommand.class,
                KafkaCreateAllSchemasCommand.class
        })
public class KafkaCLI { }
