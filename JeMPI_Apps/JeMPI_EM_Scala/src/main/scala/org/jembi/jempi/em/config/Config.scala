package org.jembi.jempi.em.configuration

case class auxField(
    fieldName: String,
    fieldType: String,
    csvCol: Option[Int],
    index: Option[String],
    isList: Option[Boolean],
    source: Option[Source],
    default: Option[String]
)

case class AdditionalNode(nodeName: String, fields: Array[AdditionalNodeField])

case class AdditionalNodeField(
    fieldName: String,
    fieldType: String,
    source: Option[Source]
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

case class Source(
    generate: Option[Generate],
    csvCol: Option[Int],
    interactionField: Option[String]
)

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

case class ValidateRules(deterministic: List[Rule])

case class Rules(
    link: Option[AllRules],
    validate: Option[ValidateRules],
    matchNotification: Option[AllRules]
)

case class Config(
    auxInteractionFields: Option[Array[auxField]],
    auxGoldenRecordFields: Option[Array[auxField]],
    additionalNodes: Option[Array[AdditionalNode]],
    demographicFields: Array[DemographicField],
    rules: Rules
)
