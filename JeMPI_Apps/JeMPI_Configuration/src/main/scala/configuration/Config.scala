package configuration

case class UniqueField(fieldName: String,
                       fieldType: String,
                       csvCol: Option[Int],
                       index: Option[String],
                       isList: Option[Boolean],
                       default: Option[String])

case class AdditionalNode(nodeName: String,
                          fields: Array[AdditionalNodeField])

case class AdditionalNodeField(fieldName: String,
                               fieldType: String,
                               csvCol: Option[Int])

case class DemographicField(fieldName: String,
                            fieldType: String,
                            csvCol: Option[Int],
                            isList: Option[Boolean],
                            indexGoldenRecord: Option[String],
                            indexInteraction: Option[String],
                            validateMetaData: Option[ProbabilisticMetaData],
                            linkMetaData: Option[ProbabilisticMetaData])

case class ProbabilisticMetaData(comparison: String,
                                 comparisonLevels: List[Double],
                                 m: Double,
                                 u: Double)

case class Rule(vars: Array[String],
                text: String)

case class LinkRules(deterministic: Map[String, Rule],
                     probabilistic: Map[String, Rule])

case class ValidateRules(deterministic: Map[String, Rule])

case class Rules(link: LinkRules,
                 validate: Option[ValidateRules])

case class Config(uniqueInteractionFields: Option[Array[UniqueField]],
                  uniqueGoldenRecordFields: Option[Array[UniqueField]],
                  additionalNodes: Option[Array[AdditionalNode]],
                  demographicFields: Array[DemographicField],
                  rules: Rules)
