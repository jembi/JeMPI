package org.jembi.jempi.em

import scala.collection.immutable.ArraySeq

object Fields {

  val FIELDS: ArraySeq[Field] = ArraySeq(
    Field("Given Name", 1),
    Field("Family Name", 2),
    Field("Gender", 3),
    Field("Date of Birth", 4),
    Field("City", 5),
    Field("Mobile", 6),
    Field("National ID", 7)
  )

}
