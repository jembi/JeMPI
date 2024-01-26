package org.jembi.jempi.em

import scala.collection.immutable.ArraySeq

object CustomFields {

  val FIELDS: ArraySeq[Field] = ArraySeq(
    Field("givenName", 0),
    Field("familyName", 1),
    Field("gender", 2),
    Field("dob", 3),
    Field("city", 4),
    Field("phoneNumber", 5),
    Field("nationalId", 6)
  )

}

