import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class InteractionEnvelop(contentType: String,
                              tag: Option[String],
                              stan: Option[String],
                              interaction: Option[Interaction]) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class Interaction(uniqueInteractionData: UniqueInteractionData,
                       demographicData: DemographicData)

@JsonIgnoreProperties(ignoreUnknown = true)
case class UniqueInteractionData(auxId: String)


@JsonIgnoreProperties(ignoreUnknown = true)
case class DemographicData(givenName: String,
                           familyName: String,
                           gender: String,
                           dob: String,
                           city: String,
                           phoneNumber: String,
                           nationalId: String)

/*
{
  "contentType":"BATCH_INTERACTION",
  "tag":"csv/test-data-0002000-10-20.csv",
  "stan":"2023/11/26 13:04:15:0020373",
  "interaction":{
    "sourceId":{
      "facility":"MA5",
      "patient":"197304015001145"
    },
    "uniqueInteractionData":{
      "auxDateCreated":"2023-11-26T13:04:34.086380562",
      "auxId":"rec-0000001678--1",
      "auxClinicalData":"RANDOM DATA(160)"
    },
    "demographicData":{
      "givenName":"david",
      "familyName":"garcia",
      "gender":"male",
      "dob":"19730401",
      "city":"nashville",
      "phoneNumber":"0434162342",
      "nationalId":"197304015001145"
    }
  }
}
 */
