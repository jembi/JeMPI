package configuration

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}

import java.nio.file.Paths

object Main {


  @main def configure(in_config_file_name: String): Any =

    val config_file_name = if (in_config_file_name.isBlank) {
      println("Dude: you should specify a config file name")
      "config_reference.json"
    } else {
      in_config_file_name
    }
    println(s"name =  ${config_file_name}")

    val mapper = JsonMapper.builder().addModule(DefaultScalaModule).build() :: ClassTagExtensions
    val config = mapper.readValue(Paths.get(config_file_name).toFile, new TypeReference[Config] {})

    CustomPatient.generateDemographicData(config.commonFields)
    CustomMU.generate(config.commonFields)
    CustomDgraphConstants.generate(config.commonFields)
    CustomDgraphInteraction.generate(config.commonFields)
    CustomDgraphReverseGoldenRecord.generate(config.commonFields)
    CustomDgraphGoldenRecord.generate(config.commonFields)
    CustomDgraphExpandedGoldenRecord.generate(config.commonFields)
    CustomDgraphExpandedInteraction.generate(config.commonFields)
    CustomDgraphMutations.generate(config.commonFields)
    CustomDgraphQueries.parseRules(config)
    CustomLinkerDeterministic.parseRules(config)
    CustomLinkerProbabilistic.parseRules(config)
    CustomLinkerBackEnd.parseRules(config)
    CustomLinkerMU.parseRules(config)

}
