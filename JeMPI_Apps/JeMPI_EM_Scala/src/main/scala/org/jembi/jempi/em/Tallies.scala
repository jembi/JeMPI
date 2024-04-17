package org.jembi.jempi.em

import scala.collection.immutable.ArraySeq

case class Tallies(colTally: ArraySeq[Tally]) {

  def this(n: Int) = this(ArraySeq.range(0, n).map(_ => Tally()))

}
