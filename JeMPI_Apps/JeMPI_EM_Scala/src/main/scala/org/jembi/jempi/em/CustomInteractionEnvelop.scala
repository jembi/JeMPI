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
case class DemographicData(
    givenName: String,
    familyName: String,
    gender: String,
    dob: String,
    city: String,
    phoneNumber: String,
    nationalId: String
) {

   def toArray: Array[String] =
      Array(givenName,
            familyName,
            gender,
            dob,
            city,
            phoneNumber,
            nationalId)

}

