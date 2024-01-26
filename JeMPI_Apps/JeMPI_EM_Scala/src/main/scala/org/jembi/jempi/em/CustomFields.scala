package org.jembi.jempi.em

import scala.collection.immutable.ArraySeq

object CustomFields {

  val FIELDS: ArraySeq[Field] = ArraySeq(
    Field("Given Name", 0),
    Field("Family Name", 1),
    Field("Gender", 2),
    Field("Date of Birth", 3),
    Field("City", 4),
    Field("Mobile", 5),
    Field("National ID", 6)
  )

}

