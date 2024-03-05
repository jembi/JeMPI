package org.jembi.jempi.em

import CustomFields.FIELDS
import scala.collection.immutable.ArraySeq

case class Tallies(colTally: ArraySeq[Tally] = CustomFields.LINK_COLS.map(_ => Tally()))
