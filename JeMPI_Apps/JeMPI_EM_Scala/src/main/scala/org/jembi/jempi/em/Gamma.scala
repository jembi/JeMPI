package org.jembi.jempi.em

import scala.annotation.tailrec
import scala.collection.immutable.ArraySeq
import scala.collection.parallel.immutable.ParVector

object Gamma {

  @tailrec
  def getGamma(
      cols: ArraySeq[Int],
      gamma: Map[String, Long],
      left: ArraySeq[String],
      right: ParVector[ArraySeq[String]]
  ): Map[String, Long] = {

    def innerLoop(
        left: ArraySeq[String],
        interactions: ParVector[ArraySeq[String]]
    ): Map[String, Long] = {

      def combineOp(
          m1: Map[String, Long],
          m2: Map[String, Long]
      ): Map[String, Long] = {
        m1 ++ m2.map { case (k: String, v: Long) =>
          k -> (v + m1.getOrElse(k, 0L))
        }
      }

      def sequenceOp(m1: Map[String, Long], t: String): Map[String, Long] = {
        m1 ++ Map(t -> (1L + m1.getOrElse(t, 0L)))
      }

      val gamma: ParVector[String] =
        interactions.map(right => gammaKey(cols, left, right))
      gamma.aggregate(Map[String, Long]())(sequenceOp, combineOp)
    }

    if (right.isEmpty) {
      gamma
    } else {
      getGamma(
        cols,
        gamma ++ innerLoop(left, right)
          .map { case (k: String, v: Long) =>
            k -> (v + gamma.getOrElse(k, 0L))
          },
        right.head,
        right.tail
      )
    }
  }

  private def gammaKey(
      cols : ArraySeq[Int],
      left: ArraySeq[String],
      right: ArraySeq[String]
  ): String = {
    val left_ = cols.map(i => left.apply(i))
    val right_ = cols.map(i => right.apply(i))
    val key: ArraySeq[Int] = (left_ zip right_).map { case (l, r) =>
      if (l.isEmpty || r.isEmpty) {
        Utils.GAMMA_TAG_MISSING
      } else if (l.equals(r)) {
        Utils.GAMMA_TAG_EQUAL
      } else {
        Utils.GAMMA_TAG_NOT_EQUAL
      }
    }
    key.mkString("<", ",", ">")
  }

}
