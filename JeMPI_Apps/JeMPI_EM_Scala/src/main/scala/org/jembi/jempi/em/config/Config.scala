package org.jembi.jempi.em.configuration

case class UniqueField(
    fieldName: String,
    fieldType: String,
    csvCol: Option[Int],
    index: Option[String],
    isList: Option[Boolean],
    source: Option[String],
    default: Option[String]
)

case class AdditionalNode(nodeName: String, fields: Array[AdditionalNodeField])

case class AdditionalNodeField(
    fieldName: String,
    fieldType: String,
    csvCol: Option[Int]
)

case class DemographicField(
    fieldName: String,
    fieldType: String,
    isList: Option[Boolean],
    source: Option[Source],
    indexGoldenRecord: Option[String],
    indexInteraction: Option[String],
    linkMetaData: Option[ProbabilisticMetaData],
    validateMetaData: Option[ProbabilisticMetaData],
    matchMetaData: Option[ProbabilisticMetaData]
)

case class Source(generate: Option[Generate], csvCol: Option[Int])

case class Generate(func: String)

case class ProbabilisticMetaData(
    comparison: String,
    comparisonLevels: List[Double],
    m: Double,
    u: Double
)

case class Rule(vars: Array[String], text: String)

case class AllRules(
    deterministic: Option[List[Rule]],
    probabilistic: Option[List[Rule]]
)

case class ValidateRules(deterministic: Map[String, Rule])

case class Rules(
    link: Option[AllRules],
    validate: Option[ValidateRules],
    matchNotification: Option[AllRules]
)

case class Config(
    uniqueInteractionFields: Option[Array[UniqueField]],
    uniqueGoldenRecordFields: Option[Array[UniqueField]],
    additionalNodes: Option[Array[AdditionalNode]],
    demographicFields: Array[DemographicField],
    rules: Rules
)
