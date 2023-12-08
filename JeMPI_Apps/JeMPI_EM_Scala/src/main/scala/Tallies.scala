import Fields.FIELDS

import scala.collection.immutable.ArraySeq

case class Tallies(colTally: ArraySeq[Tally] = FIELDS.map(_ => Tally()))
