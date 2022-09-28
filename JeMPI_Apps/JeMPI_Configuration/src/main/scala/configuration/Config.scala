package org.jembi.jempi
package configuration

case class Field(fieldName: String,
                 fieldType: String,
                 isList: Option[Boolean],
                 indexGoldenRecord: Option[String],
                 indexEntity: Option[String],
                 m: Option[Double],
                 u: Option[Double])

case class Rule(vars: Array[String],
                text: String)

case class Rules(deterministic: Map[String, Rule],
                 probabilistic: Map[String, Rule])

case class Config(fields: Array[Field],
                  rules: Rules)


