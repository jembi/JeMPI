package configuration

object Utils {

  val javaType: Map[String, String] = Map.apply("String" -> "String", "Bool" -> "Boolean", "DateTime" -> "java.time.LocalDateTime")

  def camelCaseToSnakeCase(name: String): String = "[A-Z\\d]".r.replaceAllIn(name, {
    m => "_" + m.group(0).toLowerCase()
  })

  def snakeCaseToCamelCase(name: String): String = "_([a-z\\d])".r.replaceAllIn(name, {
    m => m.group(1).toUpperCase()
  })

}
