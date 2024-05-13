package org.jembi.jempi.em

import scala.collection.immutable.ArraySeq

case class FieldsConfig(fields : ArraySeq[Field],
                        linkCols: ArraySeq[Int],
                        validateCols: ArraySeq[Int],
                        matchCols: ArraySeq[Int]) {
}

