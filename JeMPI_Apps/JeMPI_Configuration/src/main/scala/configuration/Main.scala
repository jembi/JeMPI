package configuration

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}

import java.nio.file.Paths

object Main {

  @main def configure(in_config_file_name: String): Any =

    val config_file_name = if (in_config_file_name.isBlank) {
      println("Dude: you should specify a config file name")
      "reference/config-reference.json"
    } else {
      in_config_file_name
    }
    println(s"name =  $config_file_name")

    val mapper = JsonMapper
      .builder()
      .addModule(DefaultScalaModule)
      .build() :: ClassTagExtensions
    val config = mapper.readValue(
      Paths.get(config_file_name).toFile,
      new TypeReference[Config] {}
    )

    CustomMU.generate(config.demographicFields)
    CustomDgraphConstants.generate(config)
    CustomDgraphInteraction.generate(config)
    CustomDgraphReverseGoldenRecord.generate(config)
    CustomDgraphGoldenRecord.generate(config)
    CustomDgraphExpandedGoldenRecord.generate(config)
    CustomDgraphExpandedInteraction.generate(config)
    CustomDgraphMutations.generate(config)
    CustomDgraphQueries.generate(config)
    CustomLinkerDeterministic.generate(config)
    CustomLinkerProbabilistic.generate(config)
    CustomLinkerBackEnd.generate(config)
    CustomLinkerMU.generate(config)
    CustomPostgresqlInteraction.generate(config.demographicFields)
    CustomPostgresqlGoldenRecord.generate(config.demographicFields)
    CustomAsyncHelper.generate(config)
    CustomPatient.generate(config)

}
