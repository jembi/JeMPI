package configuration

case class Field(fieldName: String,
                 fieldType: String,
                 isList: Option[Boolean],
                 indexGoldenRecord: Option[String],
                 indexEntity: Option[String],
                 m: Option[Double],
                 u: Option[Double],
                 fieldLabel: Option[String],
                 groups: List[Option[String]],
                 scope: List[Option[String]],
                 accessLevel: List[Option[String]])

case class SystemField(fieldName: String,
                       fieldType: String,
                       fieldLabel: String,
                       groups: List[Option[String]],
                       scope: List[Option[String]],
                       accessLevel: List[Option[String]])

case class Rule(vars: Array[String],
                text: String)

case class Rules(deterministic: Map[String, Rule],
                 probabilistic: Map[String, Rule])

case class Config(fields: Array[Field],
                  systemFields: Option[Array[SystemField]],
                  rules: Rules)


