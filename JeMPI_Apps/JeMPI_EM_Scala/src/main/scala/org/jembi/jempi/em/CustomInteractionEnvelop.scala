package org.jembi.jempi.em


import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class CustomInteractionEnvelop(
    contentType: String,
    tag: Option[String],
    stan: Option[String],
    interaction: Option[Interaction]
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
case class Interaction(
    uniqueInteractionData: UniqueInteractionData,
    demographicData: DemographicData
)

@JsonIgnoreProperties(ignoreUnknown = true)
case class UniqueInteractionData(auxId: String)


@JsonIgnoreProperties(ignoreUnknown = true)
case class MyField(tag: String, value: String)

@JsonIgnoreProperties(ignoreUnknown = true)
case class DemographicData(fields: Array[MyField]) {

   def toArray: Array[String] = fields.map(f => f.value)

}

